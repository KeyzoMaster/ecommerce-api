package com.lemzo.ecommerce.security.infrastructure.ratelimit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import java.util.logging.Logger;

/**
 * Service de limitation de débit (Sliding Window simple).
 */
@ApplicationScoped
public class RateLimitService {

    private static final Logger LOGGER = Logger.getLogger(RateLimitService.class.getName());
    private static final String KEY_PREFIX = "ratelimit:";
    private static final long WINDOW_SECONDS = 60;

    @Inject
    private JedisPool jedisPool;

    public boolean isAllowed(String key, int maxRequests) {
        long currentWindow = System.currentTimeMillis() / 1000 / WINDOW_SECONDS;
        String redisKey = KEY_PREFIX + key + ":" + currentWindow;

        try (Jedis jedis = jedisPool.getResource()) {
            Long count = jedis.incr(redisKey);
            if (count == 1) {
                jedis.expire(redisKey, WINDOW_SECONDS * 2);
            }
            return count <= maxRequests;
        } catch (Exception e) {
            LOGGER.severe("Erreur RateLimit Redis: " + e.getMessage());
            return true; // Fail-Open par défaut pour ne pas bloquer les utilisateurs
        }
    }
}
