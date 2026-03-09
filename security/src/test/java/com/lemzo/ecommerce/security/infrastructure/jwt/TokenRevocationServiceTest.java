package com.lemzo.ecommerce.security.infrastructure.jwt;

import com.lemzo.ecommerce.storage.infrastructure.redis.JedisPoolProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour TokenRevocationService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRevocationService Unit Tests")
class TokenRevocationServiceTest {

    @Mock
    private JedisPoolProvider poolProvider;

    @Mock
    private Jedis jedis;

    @InjectMocks
    private TokenRevocationService revocationService;

    @Test
    @DisplayName("Should revoke token by saving JTI in Redis")
    void shouldRevokeToken() {
        // Arrange
        final String jti = "test-jti-123";
        final long ttl = 3600;
        when(poolProvider.getResource()).thenReturn(jedis);

        // Act
        revocationService.revoke(jti, ttl);

        // Assert
        verify(jedis).setex(eq("revoked_token:" + jti), eq(ttl), eq("true"));
    }

    @Test
    @DisplayName("Should return true if token is revoked in Redis")
    void shouldReturnTrueIfRevoked() {
        // Arrange
        final String jti = "revoked-jti";
        when(poolProvider.getResource()).thenReturn(jedis);
        when(jedis.exists("revoked_token:" + jti)).thenReturn(true);

        // Act
        final boolean isRevoked = revocationService.isRevoked(jti);

        // Assert
        assertTrue(isRevoked);
    }
}
