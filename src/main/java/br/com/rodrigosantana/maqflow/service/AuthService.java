package br.com.rodrigosantana.maqflow.service;

import br.com.rodrigosantana.maqflow.dto.auth.*;
import br.com.rodrigosantana.maqflow.entity.RefreshToken;
import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.exception.AuthenticationFailedException;
import br.com.rodrigosantana.maqflow.exception.InvalidRefreshTokenException;
import br.com.rodrigosantana.maqflow.exception.RefreshTokenReuseException;
import br.com.rodrigosantana.maqflow.repository.RefreshTokenRepository;
import br.com.rodrigosantana.maqflow.repository.UserRepository;
import br.com.rodrigosantana.maqflow.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository users;
    private final RefreshTokenRepository tokens;
    private final JwtService jwt;
    private final long refreshExpirationMs;
    private final SecureRandom random = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager, UserRepository users,
                       RefreshTokenRepository tokens, JwtService jwt,
                       @Value("${security.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.tokens = tokens;
        this.jwt = jwt;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException exception) {
            throw new AuthenticationFailedException();
        }
        User user = users.findByEmail(request.email()).orElseThrow(AuthenticationFailedException::new);
        return issue(user, UUID.randomUUID());
    }

    @Transactional(noRollbackFor = RefreshTokenReuseException.class)
    public AuthResponseDTO refresh(String presentedToken) {
        Instant now = Instant.now();
        RefreshToken old = tokens.findByTokenHashForUpdate(hash(presentedToken)).orElseThrow(InvalidRefreshTokenException::new);
        if (old.isRevoked()) {
            tokens.findAllByFamilyId(old.getFamilyId()).forEach(token -> {
                if (!token.isRevoked()) token.revoke(now, null);
            });
            throw new RefreshTokenReuseException();
        }
        if (!old.getExpiresAt().isAfter(now)) {
            old.revoke(now, null);
            throw new InvalidRefreshTokenException();
        }
        User user = users.findById(old.getUser().getId()).orElseThrow(InvalidRefreshTokenException::new);
        String replacement = newRefreshToken();
        old.revoke(now, hash(replacement));
        tokens.save(createToken(user, replacement, old.getFamilyId(), now));
        return response(user, replacement);
    }

    @Transactional
    public void logout(String presentedToken) {
        tokens.findByTokenHashForUpdate(hash(presentedToken)).ifPresent(token -> {
            if (!token.isRevoked()) token.revoke(Instant.now(), null);
        });
    }

    @Transactional(readOnly = true)
    public UserAuthResponseDTO me(UUID id) {
        User user = users.findById(id).orElseThrow(AuthenticationFailedException::new);
        return toUser(user);
    }

    private AuthResponseDTO issue(User user, UUID familyId) {
        String raw = newRefreshToken();
        tokens.save(createToken(user, raw, familyId, Instant.now()));
        return response(user, raw);
    }

    private AuthResponseDTO response(User user, String refreshToken) {
        return new AuthResponseDTO(jwt.generate(user.getId()), refreshToken, "Bearer",
                jwt.getAccessExpirationSeconds(), refreshExpirationMs / 1000, toUser(user));
    }

    private UserAuthResponseDTO toUser(User user) {
        return new UserAuthResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private RefreshToken createToken(User user, String raw, UUID familyId, Instant now) {
        return new RefreshToken(user, hash(raw), familyId, now, now.plusMillis(refreshExpirationMs));
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível processar o token.");
        }
    }
}
