package com.lemzo.ecommerce.security.infrastructure.jwt;

import com.lemzo.ecommerce.storage.infrastructure.redis.JedisPoolProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service de révocation de tokens utilisant Redis.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TokenRevocationService {

    private static final Logger LOGGER = Logger.getLogger(TokenRevocationService.class.getName());
    private static final String REVOCATION_KEY_PREFIX = "revoked_token:";

    private final JedisPoolProvider jedisPoolProvider;

    public void revoke(final String jti, final long ttlSeconds) {
        try (Jedis jedis = jedisPoolProvider.getResource()) {
            jedis.setex(REVOCATION_KEY_PREFIX + jti, ttlSeconds, "true");
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(() -> "Token révoqué dans Redis: " + jti);
            }
        } catch (final Exception exception) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la révocation du token dans Redis", exception);
        }
    }

    public boolean isRevoked(final String jti) {
        try (Jedis jedis = jedisPoolProvider.getResource()) {
            return jedis.exists(REVOCATION_KEY_PREFIX + jti);
        } catch (final Exception exception) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification de la révocation", exception);
            return false;
        }
    }
}
