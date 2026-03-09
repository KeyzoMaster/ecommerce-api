package com.lemzo.ecommerce.storage.infrastructure.redis;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Configuration Redis.
 * Le pool est désormais géré par JedisPoolProvider pour des raisons de conformité CDI.
 */
@ApplicationScoped
public class RedisConfig {
    public RedisConfig() {
        // Required by CDI
    }
}
