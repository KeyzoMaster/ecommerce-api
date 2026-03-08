package com.lemzo.ecommerce.domain.sales.api.dto;

import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.domain.Order.OrderStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Détails d'une commande")
public record OrderResponse(
    @Schema(description = "Identifiant unique")
    UUID id,

    @Schema(description = "Numéro de commande lisible", example = "ORD-2025-ABCD")
    String orderNumber,

    @Schema(description = "Statut actuel")
    OrderStatus status,

    @Schema(description = "Prix total (calculé par la DB)", example = "1250.50")
    BigDecimal totalPrice,

    @Schema(description = "Devise", example = "EUR")
    String currency,

    @Schema(description = "URL de redirection pour le paiement (si applicable)")
    String paymentUrl
) {
    public static OrderResponse from(Order order, String paymentUrl) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getTotalPrice(),
            order.getCurrency(),
            paymentUrl
        );
    }
}
