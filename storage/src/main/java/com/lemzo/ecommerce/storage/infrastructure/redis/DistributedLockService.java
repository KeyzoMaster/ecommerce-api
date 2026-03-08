package com.lemzo.ecommerce.storage.infrastructure.redis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Service de verrouillage distribué basé sur Redis (Jedis).
 */
@ApplicationScoped
public class DistributedLockService {

    private static final Logger LOGGER = Logger.getLogger(DistributedLockService.class.getName());
    private static final String LOCK_PREFIX = "lock:";

    @Inject
    private JedisPool jedisPool;

    /**
     * Exécute une action sous un verrou distribué.
     */
    public <T> T executeWithLock(String lockKey, Duration timeout, Supplier<T> action) {
        String key = LOCK_PREFIX + lockKey;
        
        try (Jedis jedis = jedisPool.getResource()) {
            // NX: Set if not exists, PX: Expiry in milliseconds
            String result = jedis.set(key, "LOCKED", SetParams.setParams().nx().px(timeout.toMillis()));

            if (!"OK".equals(result)) {
                LOGGER.warning("Impossible d'acquérir le verrou: " + key);
                throw new RuntimeException("Ressource occupée, veuillez réessayer plus tard.");
            }

            try {
                return action.get();
            } finally {
                jedis.del(key);
            }
        }
    }
}
