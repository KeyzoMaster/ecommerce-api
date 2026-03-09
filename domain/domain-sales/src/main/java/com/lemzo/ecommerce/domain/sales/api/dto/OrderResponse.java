package com.lemzo.ecommerce.domain.sales.api.dto;

import com.lemzo.ecommerce.domain.sales.domain.Order;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Réponse pour une commande.
 */
public record OrderResponse(
    UUID id,
    String orderNumber,
    OffsetDateTime orderDate,
    String status,
    BigDecimal totalAmount,
    String paymentUrl
) {
    public static OrderResponse from(final Order order) {
        return from(order, null);
    }

    public static OrderResponse from(final Order order, final String paymentUrl) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getCreatedAt(),
            order.getStatus().name(),
            order.getTotalAmount(),
            paymentUrl
        );
    }
}
