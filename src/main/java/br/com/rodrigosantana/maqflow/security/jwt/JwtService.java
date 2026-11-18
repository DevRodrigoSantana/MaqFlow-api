package br.com.rodrigosantana.maqflow.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {
    private final SecretKey key;
    private final long accessExpirationMs;

    public JwtService(@Value("${security.jwt.secret-base64}") String secret,
                      @Value("${security.jwt.access-expiration-ms:900000}") long accessExpirationMs) {
        byte[] decoded = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(decoded);
        this.accessExpirationMs = accessExpirationMs;
    }

    public String generate(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder().subject(userId.toString()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpirationMs)))
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    public UUID validateAndGetUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null || !claims.getExpiration().after(new Date())) {
            throw new IllegalArgumentException("Token expirado.");
        }
        return UUID.fromString(claims.getSubject());
    }

    public long getAccessExpirationSeconds() { return accessExpirationMs / 1000; }
}
