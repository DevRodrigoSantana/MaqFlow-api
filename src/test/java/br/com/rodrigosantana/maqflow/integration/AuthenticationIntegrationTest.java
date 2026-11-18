package br.com.rodrigosantana.maqflow.integration;

import br.com.rodrigosantana.maqflow.repository.RefreshTokenRepository;
import br.com.rodrigosantana.maqflow.repository.UserRepository;
import br.com.rodrigosantana.maqflow.entity.RefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired UserRepository users;

    @Test
    void registrationLoginJwtAccessAndMeWorkEndToEnd() throws Exception {
        String email = "auth-" + UUID.randomUUID() + "@example.test";
        register(email);

        mvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get("/api/v1/users").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());

        MvcResult login = login(email, "ValidPass1!");
        String access = jsonValue(login, "accessToken");
        String refresh = jsonValue(login, "refreshToken");
        assertTrue(refreshTokens.findAll().stream().noneMatch(token -> token.getTokenHash().equals(refresh)));
        assertEquals("Bearer", jsonValue(login, "tokenType"));
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + access))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsInvalidCredentialsWithoutRevealingWhetherEmailExists() throws Exception {
        String email = "auth-invalid-" + UUID.randomUUID() + "@example.test";
        register(email);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, "WrongPass1!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciais inválidas."));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("missing-" + email, "WrongPass1!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciais inválidas."));
    }

    @Test
    void rotatesRefreshTokenAndRevokesFamilyWhenOldTokenIsReused() throws Exception {
        String email = "auth-refresh-" + UUID.randomUUID() + "@example.test";
        register(email);
        String first = jsonValue(login(email, "ValidPass1!"), "refreshToken");
        MvcResult rotated = mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + first + "\"}"))
                .andExpect(status().isOk()).andReturn();
        String second = jsonValue(rotated, "refreshToken");
        assertNotEquals(first, second);

        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + first + "\"}"))
                .andExpect(status().isUnauthorized());
        UUID familyId = refreshTokens.findByTokenHash(sha256(first)).orElseThrow().getFamilyId();
        assertTrue(refreshTokens.findAllByFamilyId(familyId).stream().allMatch(token -> token.isRevoked()));

        mvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"unknown-token\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutRevokesThePresentedRefreshToken() throws Exception {
        String email = "auth-logout-" + UUID.randomUUID() + "@example.test";
        register(email);
        String refresh = jsonValue(login(email, "ValidPass1!"), "refreshToken");
        mvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectsExpiredRefreshToken() throws Exception {
        String email = "auth-expired-" + UUID.randomUUID() + "@example.test";
        register(email);
        String raw = "expired-refresh-token";
        Instant past = Instant.now().minusSeconds(60);
        RefreshToken expired = new RefreshToken(users.findByEmail(email).orElseThrow(), sha256(raw), UUID.randomUUID(),
                past.minusSeconds(60), past);
        refreshTokens.saveAndFlush(expired);

        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + raw + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    private void register(String email) throws Exception {
        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Auth Test\",\"email\":\"" + email + "\",\"password\":\"ValidPass1!\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist());
    }

    private MvcResult login(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, password)))
                .andExpect(status().isOk()).andReturn();
    }

    private String loginBody(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String sha256(String token) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    private String jsonValue(MvcResult result, String name) throws Exception {
        Matcher matcher = Pattern.compile("\\\"" + name + "\\\":\\\"([^\\\"]+)\\\"").matcher(result.getResponse().getContentAsString());
        assertTrue(matcher.find(), "Expected JSON property " + name);
        return matcher.group(1);
    }
}
