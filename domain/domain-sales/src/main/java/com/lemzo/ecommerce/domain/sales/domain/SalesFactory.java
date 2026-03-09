package com.lemzo.ecommerce.domain.sales.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Factory pour les entités du domaine Sales.
 */
public final class SalesFactory {

    private SalesFactory() {
        // Classe utilitaire
    }

    /**
     * Crée une nouvelle commande dans l'état initial.
     */
    public static Order createOrder(final UUID userId, final String orderNumber) {
        final Order order = new Order(userId, orderNumber);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setItems(new ArrayList<>());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setShippingCost(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        return order;
    }

    /**
     * Crée un élément de commande.
     */
    public static OrderItem createItem(final UUID productId, final UUID categoryId, 
                                       final int quantity, final BigDecimal unitPrice) {
        return new OrderItem(productId, categoryId, quantity, unitPrice, BigDecimal.ZERO, null);
    }
}
