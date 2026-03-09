package com.lemzo.ecommerce.storage.infrastructure.redis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Service de verrouillage distribué utilisant Redis.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DistributedLockService {

    private final JedisPoolProvider jedisPoolProvider;

    /**
     * Exécute une action sous un verrou distribué.
     */
    public <T> Optional<T> withLock(String lockKey, long timeoutSeconds, Supplier<T> action) {
        var lockValue = UUID.randomUUID().toString();
        var params = SetParams.setParams().nx().ex(timeoutSeconds);

        try (var jedis = jedisPoolProvider.getResource()) {
            var result = jedis.set(lockKey, lockValue, params);
            
            return Optional.ofNullable(result)
                    .filter(res -> res.equals("OK"))
                    .map(res -> executeAndRelease(jedis, lockKey, lockValue, action));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private <T> T executeAndRelease(Jedis jedis, String key, String value, Supplier<T> action) {
        try {
            return action.get();
        } finally {
            // Libération sécurisée : on ne supprime que si on possède le verrou
            Optional.ofNullable(jedis.get(key))
                    .filter(value::equals)
                    .ifPresent(v -> jedis.del(key));
        }
    }
}
