package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import com.lemzo.ecommerce.domain.sales.domain.Cart;
import com.lemzo.ecommerce.domain.sales.domain.CartItem;
import com.lemzo.ecommerce.domain.sales.repository.CartRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de gestion du panier.
 */
@ApplicationScoped
public class CartService {

    @Inject
    private CartRepository cartRepository;

    @Inject
    private ProductRepository productRepository;

    public Cart getCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> new Cart(userId, new ArrayList<>()));
    }

    public Cart addItem(UUID userId, UUID productId, int quantity) {
        Cart currentCart = getCart(userId);
        
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));

        List<CartItem> newItems = new ArrayList<>(currentCart.items());
        
        // Si le produit existe déjà, on met à jour la quantité, sinon on l'ajoute
        boolean exists = newItems.stream()
                .filter(item -> item.productId().equals(productId))
                .findFirst()
                .map(item -> {
                    newItems.remove(item);
                    newItems.add(new CartItem(productId, product.getName(), item.quantity() + quantity, product.getPrice()));
                    return true;
                }).orElse(false);

        if (!exists) {
            newItems.add(new CartItem(productId, product.getName(), quantity, product.getPrice()));
        }

        Cart updatedCart = new Cart(userId, List.copyOf(newItems));
        cartRepository.save(updatedCart);
        return updatedCart;
    }

    public Cart removeItem(UUID userId, UUID productId) {
        Cart currentCart = getCart(userId);
        List<CartItem> newItems = currentCart.items().stream()
                .filter(item -> !item.productId().equals(productId))
                .collect(Collectors.toList());

        Cart updatedCart = new Cart(userId, List.copyOf(newItems));
        cartRepository.save(updatedCart);
        return updatedCart;
    }

    public void clearCart(UUID userId) {
        cartRepository.deleteByUserId(userId);
    }
}
