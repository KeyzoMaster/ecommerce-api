package com.lemzo.ecommerce.security.infrastructure.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour JwtService (Minimaliste pour HATEOAS).
 */
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "votre_secret_tres_long_et_securise_pour_jwt_test_123456";
    private final long expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate and validate a minimal token correctly")
    void shouldGenerateAndValidateToken() {
        // Arrange
        final UUID userId = UUID.randomUUID();
        final String email = "test@example.com";

        // Act
        final String token = jwtService.generateToken(userId, email);
        final var claims = jwtService.validateToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(email, claims.get("email"));
        
        final UUID userIdFromToken = jwtService.getUserIdFromToken(token);
        assertEquals(userId, userIdFromToken);
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void shouldFailForInvalidToken() {
        assertThrows(Exception.class, () -> jwtService.validateToken("invalid.token.here"));
    }
}
