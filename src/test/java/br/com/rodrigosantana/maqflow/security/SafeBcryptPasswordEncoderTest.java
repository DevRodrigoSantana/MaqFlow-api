package br.com.rodrigosantana.maqflow.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SafeBcryptPasswordEncoderTest {
    private final SafeBcryptPasswordEncoder encoder = new SafeBcryptPasswordEncoder();

    @Test
    void encodesAndVerifiesPasswordsUsingBcrypt() {
        String raw = "SafePass1!";
        String hash = encoder.encode(raw);
        assertTrue(hash.startsWith("$2"));
        assertNotEquals(raw, hash);
        assertTrue(encoder.matches(raw, hash));
    }

    @Test
    void rejectsLegacyPlaintextWithoutComparingItAsAHash() {
        assertFalse(encoder.matches("legacy-password", "legacy-password"));
    }
}
