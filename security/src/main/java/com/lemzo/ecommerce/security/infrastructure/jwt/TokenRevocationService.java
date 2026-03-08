package com.lemzo.ecommerce.security.infrastructure.jwt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.logging.Logger;

/**
 * Service de révocation de tokens JWT utilisant Redis.
 */
@ApplicationScoped
public class TokenRevocationService {

    private static final Logger LOGGER = Logger.getLogger(TokenRevocationService.class.getName());
    private static final String REVOCATION_KEY_PREFIX = "token:revoked:";

    @Inject
    private JedisPool jedisPool;

    /**
     * Révoque un token en ajoutant son JTI à Redis.
     */
    public void revoke(String jti, long ttlSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(REVOCATION_KEY_PREFIX + jti, ttlSeconds, "1");
            LOGGER.info("Token révoqué : " + jti);
        } catch (Exception e) {
            LOGGER.severe("Erreur révocation token Redis: " + e.getMessage());
        }
    }

    /**
     * Vérifie si un token est révoqué.
     */
    public boolean isRevoked(String jti) {
        if (jti == null) return false;
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(REVOCATION_KEY_PREFIX + jti);
        } catch (Exception e) {
            LOGGER.severe("Erreur vérification révocation Redis: " + e.getMessage());
            return false; // Fail-Safe
        }
    }
}
