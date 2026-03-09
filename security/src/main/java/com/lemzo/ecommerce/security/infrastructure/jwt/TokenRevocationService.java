package com.lemzo.ecommerce.security.infrastructure.jwt;

import com.lemzo.ecommerce.storage.infrastructure.redis.JedisPoolProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import java.util.logging.Logger;
import java.util.Optional;

/**
 * Service de révocation de tokens JWT utilisant Redis.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TokenRevocationService {

    private static final Logger LOGGER = Logger.getLogger(TokenRevocationService.class.getName());
    private static final String REVOCATION_KEY_PREFIX = "token:revoked:";

    private final JedisPoolProvider jedisPoolProvider;

    /**
     * Révoque un token en ajoutant son JTI à Redis.
     */
    public void revoke(String jti, long ttlSeconds) {
        try (var jedis = jedisPoolProvider.getResource()) {
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
        return Optional.ofNullable(jti)
                .map(id -> checkInRedis(id))
                .orElse(false);
    }

    private boolean checkInRedis(String jti) {
        try (var jedis = jedisPoolProvider.getResource()) {
            return jedis.exists(REVOCATION_KEY_PREFIX + jti);
        } catch (Exception e) {
            LOGGER.severe("Erreur vérification révocation Redis: " + e.getMessage());
            return false;
        }
    }
}
