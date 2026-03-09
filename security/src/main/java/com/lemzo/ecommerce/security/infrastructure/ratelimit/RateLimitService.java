package com.lemzo.ecommerce.security.infrastructure.ratelimit;

import com.lemzo.ecommerce.storage.infrastructure.redis.JedisPoolProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.logging.Logger;
import java.util.Optional;

/**
 * Service de limitation de débit (Sliding Window simple).
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RateLimitService {

    private static final Logger LOGGER = Logger.getLogger(RateLimitService.class.getName());
    private static final String KEY_PREFIX = "ratelimit:";
    private static final long WINDOW_SECONDS = 60;

    private final JedisPoolProvider jedisPoolProvider;

    public boolean isAllowed(String key, int maxRequests) {
        var currentWindow = System.currentTimeMillis() / 1000 / WINDOW_SECONDS;
        var redisKey = KEY_PREFIX + key + ":" + currentWindow;

        try (var jedis = jedisPoolProvider.getResource()) {
            var count = jedis.incr(redisKey);
            if (count == 1) {
                jedis.expire(redisKey, WINDOW_SECONDS * 2);
            }
            return count <= maxRequests;
        } catch (Exception e) {
            LOGGER.severe("Erreur RateLimit Redis: " + e.getMessage());
            return true; // Fail-Open
        }
    }
}
