package com.lemzo.ecommerce.storage.infrastructure.redis;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.util.Optional;

/**
 * Fournisseur sécurisé de ressources Redis (Jedis).
 * Encapsule JedisPool pour éviter les erreurs de proxying CDI.
 */
@ApplicationScoped
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class JedisPoolProvider {

    private final JedisPool jedisPool;

    @Inject
    public JedisPoolProvider(
            @ConfigProperty(name = "REDIS_HOST", defaultValue = "localhost") String host,
            @ConfigProperty(name = "REDIS_PORT", defaultValue = "6379") int port) {
        
        var poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        this.jedisPool = new JedisPool(poolConfig, host, port);
    }

    /**
     * Récupère une ressource Jedis depuis le pool.
     */
    public Jedis getResource() {
        return jedisPool.getResource();
    }

    /**
     * Fermeture propre du pool au déchargement du bean.
     */
    @PreDestroy
    public void close() {
        Optional.ofNullable(jedisPool).ifPresent(JedisPool::close);
    }
}
