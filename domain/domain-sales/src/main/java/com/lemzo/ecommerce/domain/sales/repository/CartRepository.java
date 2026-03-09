package com.lemzo.ecommerce.domain.sales.repository;

import com.lemzo.ecommerce.domain.sales.domain.Cart;
import com.lemzo.ecommerce.storage.infrastructure.redis.JedisPoolProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour la gestion des paniers dans Redis.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class CartRepository {

    private static final String KEY_PREFIX = "cart:";
    private static final int EXPIRATION_SECONDS = 86400; // 24h
    
    private final JedisPoolProvider jedisPoolProvider;
    private final Jsonb jsonb = JsonbBuilder.create();

    /**
     * Sauvegarde le panier dans Redis.
     */
    public void save(Cart cart) {
        var key = KEY_PREFIX + cart.userId();
        try (var jedis = jedisPoolProvider.getResource()) {
            jedis.setex(key, EXPIRATION_SECONDS, jsonb.toJson(cart));
        }
    }

    /**
     * Récupère le panier d'un utilisateur.
     */
    public Optional<Cart> findByUserId(UUID userId) {
        var key = KEY_PREFIX + userId;
        try (var jedis = jedisPoolProvider.getResource()) {
            return Optional.ofNullable(jedis.get(key))
                    .map(data -> jsonb.fromJson(data, Cart.class));
        }
    }

    /**
     * Supprime le panier d'un utilisateur.
     */
    public void deleteByUserId(UUID userId) {
        var key = KEY_PREFIX + userId;
        try (var jedis = jedisPoolProvider.getResource()) {
            jedis.del(key);
        }
    }
}
