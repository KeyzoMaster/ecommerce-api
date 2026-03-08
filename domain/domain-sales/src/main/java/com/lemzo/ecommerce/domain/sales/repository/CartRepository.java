package com.lemzo.ecommerce.domain.sales.repository;

import com.lemzo.ecommerce.domain.sales.domain.Cart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour la gestion des paniers dans Redis.
 */
@ApplicationScoped
public class CartRepository {

    private static final String CART_PREFIX = "cart:";
    private final Jsonb jsonb = JsonbBuilder.create();

    @Inject
    private JedisPool jedisPool;

    public void save(Cart cart) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = CART_PREFIX + cart.userId();
            jedis.set(key, jsonb.toJson(cart));
            // Expire après 24 heures d'inactivité
            jedis.expire(key, 86400);
        }
    }

    public Optional<Cart> findByUserId(UUID userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String data = jedis.get(CART_PREFIX + userId);
            return Optional.ofNullable(data)
                    .map(json -> jsonb.fromJson(json, Cart.class));
        }
    }

    public void deleteByUserId(UUID userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(CART_PREFIX + userId);
        }
    }
}
