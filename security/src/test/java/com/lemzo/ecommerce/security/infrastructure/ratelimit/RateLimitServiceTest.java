package com.lemzo.ecommerce.security.infrastructure.ratelimit;

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
 * Tests unitaires pour RateLimitService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService Unit Tests")
class RateLimitServiceTest {

    @Mock
    private JedisPoolProvider poolProvider;

    @Mock
    private Jedis jedis;

    @InjectMocks
    private RateLimitService rateLimitService;

    @Test
    @DisplayName("Should allow request and set expiration on first request")
    void shouldAllowIfUnderLimit() {
        // Arrange
        when(poolProvider.getResource()).thenReturn(jedis);
        when(jedis.incr(anyString())).thenReturn(1L);

        // Act
        final boolean allowed = rateLimitService.isAllowed("127.0.0.1", 100);

        // Assert
        assertTrue(allowed);
        verify(jedis).expire(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should deny request if over limit")
    void shouldDenyIfOverLimit() {
        // Arrange
        when(poolProvider.getResource()).thenReturn(jedis);
        when(jedis.incr(anyString())).thenReturn(101L);

        // Act
        final boolean allowed = rateLimitService.isAllowed("127.0.0.1", 100);

        // Assert
        assertFalse(allowed);
    }
}
