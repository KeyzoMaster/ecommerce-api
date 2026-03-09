package com.lemzo.ecommerce.util.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour TokenService.
 */
@DisplayName("TokenService Unit Tests")
class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    @Test
    @DisplayName("Should generate secure unique tokens")
    void shouldGenerateSecureToken() {
        final var token1 = tokenService.generateSecureToken();
        final var token2 = tokenService.generateSecureToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        assertTrue(token1.length() >= 32);
    }

    @Test
    @DisplayName("Should hash tokens consistently using SHA-256")
    void shouldHashTokenConsistently() {
        final var token = "my-secret-token";
        final var hash1 = tokenService.hashToken(token);
        final var hash2 = tokenService.hashToken(token);

        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length()); // SHA-256 hex string length
    }
}
