package com.lemzo.ecommerce.storage.infrastructure.redis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class DistributedLockService {

    private final JedisPoolProvider jedisPoolProvider;

    /**
     * Exécute une action sous un verrou distribué.
     */
    public <T> Optional<T> withLock(final String lockKey, final long timeoutSeconds, final Supplier<T> action) {
        final String lockValue = UUID.randomUUID().toString();
        final SetParams params = SetParams.setParams().nx().ex(timeoutSeconds);

        try (Jedis jedis = jedisPoolProvider.getResource()) {
            final String result = jedis.set(lockKey, lockValue, params);
            
            return Optional.ofNullable(result)
                    .filter(res -> res.equals("OK"))
                    .map(res -> executeAndRelease(jedis, lockKey, lockValue, action));
        } catch (final Exception _) {
            return Optional.empty();
        }
    }

    private <T> T executeAndRelease(final Jedis jedis, final String key, final String value, final Supplier<T> action) {
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
