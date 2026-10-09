package com.example.elderai.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "test-jwt-secret-with-at-least-32-characters");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 60_000L);
        jwtUtils.validateConfiguration();
    }

    @Test
    void shouldGenerateAndParseToken() {
        String token = jwtUtils.generateToken(42L, "elder", "ELDER");

        assertTrue(jwtUtils.validateToken(token));
        assertEquals(42L, jwtUtils.getUserId(token));
        assertEquals("elder", jwtUtils.getUsername(token));
        assertEquals("ELDER", jwtUtils.getRole(token));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtUtils.generateToken(42L, "elder", "ELDER");
        String[] parts = token.split("\\.");
        char firstPayloadCharacter = parts[1].charAt(0);
        parts[1] = (firstPayloadCharacter == 'A' ? 'B' : 'A') + parts[1].substring(1);
        String tampered = String.join(".", parts);

        assertFalse(jwtUtils.validateToken(tampered));
    }

    @Test
    void shouldRejectShortSecret() {
        ReflectionTestUtils.setField(jwtUtils, "secret", "too-short");

        assertThrows(IllegalStateException.class, jwtUtils::validateConfiguration);
    }
}
