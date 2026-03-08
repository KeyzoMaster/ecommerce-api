package com.lemzo.ecommerce.storage.infrastructure.redis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@ApplicationScoped
public class RedisConfig {

    @ConfigProperty(name = "REDIS_HOST", defaultValue = "localhost")
    private String host;

    @ConfigProperty(name = "REDIS_PORT", defaultValue = "6379")
    private int port;

    @Produces
    @ApplicationScoped
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        return new JedisPool(poolConfig, host, port);
    }
}
