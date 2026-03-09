package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.domain.core.catalog.CatalogPort;
import com.lemzo.ecommerce.domain.sales.domain.Cart;
import com.lemzo.ecommerce.domain.sales.domain.CartItem;
import com.lemzo.ecommerce.domain.sales.repository.CartRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion du panier (Redis).
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CatalogPort catalogPort;

    public Optional<Cart> getCart(final UUID userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart addToCart(final UUID userId, final UUID productId, final int quantity) {
        final var currentCart = getCart(userId).orElse(new Cart(userId, new ArrayList<>()));
        
        final var product = (com.lemzo.ecommerce.domain.catalog.domain.Product) catalogPort.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));

        final var existingItem = currentCart.items().stream()
                .filter(item -> item.productId().equals(productId))
                .findFirst();

        final var newItems = new ArrayList<>(currentCart.items());
        
        existingItem.ifPresentOrElse(
            item -> {
                newItems.remove(item);
                newItems.add(new CartItem(productId, item.productName(), item.quantity() + quantity, item.unitPrice()));
            },
            () -> newItems.add(new CartItem(productId, product.getName(), quantity, product.getPrice()))
        );

        final var updatedCart = new Cart(userId, newItems);
        cartRepository.save(updatedCart);
        return updatedCart;
    }

    public Cart removeFromCart(final UUID userId, final UUID productId) {
        final var currentCart = getCart(userId).orElse(new Cart(userId, new ArrayList<>()));
        final var newItems = currentCart.items().stream()
                .filter(item -> !item.productId().equals(productId))
                .collect(Collectors.toList());

        final var updatedCart = new Cart(userId, newItems);
        cartRepository.save(updatedCart);
        return updatedCart;
    }

    public void clearCart(final UUID userId) {
        cartRepository.deleteByUserId(userId);
    }
}
