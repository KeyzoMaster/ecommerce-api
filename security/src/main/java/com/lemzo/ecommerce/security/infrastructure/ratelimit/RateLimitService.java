package com.lemzo.ecommerce.security.infrastructure.ratelimit;

import com.lemzo.ecommerce.storage.infrastructure.redis.JedisPoolProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service de limitation de débit (Rate Limiting) basé sur Redis.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class RateLimitService {

    private static final Logger LOGGER = Logger.getLogger(RateLimitService.class.getName());
    private final JedisPoolProvider jedisPoolProvider;

    public boolean isAllowed(final String key, final int maxRequests) {
        final long currentWindow = Instant.now().getEpochSecond() / 60;
        final String redisKey = "ratelimit:" + key + ":" + currentWindow;

        try (Jedis jedis = jedisPoolProvider.getResource()) {
            final long count = jedis.incr(redisKey);
            if (count == 1) {
                jedis.expire(redisKey, 60);
            }
            return count <= maxRequests;
        } catch (final Exception exception) {
            LOGGER.log(Level.SEVERE, "Erreur RateLimit Redis", exception);
            return true;
        }
    }
}
