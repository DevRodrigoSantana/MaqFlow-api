package br.com.rodrigosantana.maqflow.security;

import br.com.rodrigosantana.maqflow.security.jwt.JwtService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String KEY = "bWFxZmxvdy10ZXN0LXNlY3JldC1hdC1sZWFzdC0zMi1ieXRlcy0yMDI2";

    @Test
    void generatedAccessTokenCarriesTheUserUuidAndExpires() throws Exception {
        JwtService jwt = new JwtService(KEY, 30_000);
        UUID userId = UUID.randomUUID();
        String token = jwt.generate(userId);
        assertEquals(userId, jwt.validateAndGetUserId(token));
        assertEquals(30, jwt.getAccessExpirationSeconds());
    }

    @Test
    void rejectsExpiredAccessToken() throws Exception {
        JwtService jwt = new JwtService(KEY, 1);
        String token = jwt.generate(UUID.randomUUID());
        Thread.sleep(15);
        assertThrows(RuntimeException.class, () -> jwt.validateAndGetUserId(token));
    }

    @Test
    void rejectsTokenSignedByAnotherKey() {
        JwtService issuer = new JwtService(KEY, 30_000);
        JwtService verifier = new JwtService("YW5vdGhlci1zZWNyZXQta2V5LWF0LWxlYXN0LTMyLWJ5dGVzLTIwMjY=", 30_000);
        String token = issuer.generate(UUID.randomUUID());
        assertThrows(RuntimeException.class, () -> verifier.validateAndGetUserId(token));
    }
}
