package com.lemzo.ecommerce.domain.sales.api.dto;

import com.lemzo.ecommerce.domain.sales.domain.Order;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse pour une commande.
 */
@Schema(description = "Informations sur une commande client")
public record OrderResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Numéro de commande lisible", example = "ORD-20260309-ABCD")
    String orderNumber,
    
    @Schema(description = "Date de création de la commande")
    OffsetDateTime orderDate,
    
    @Schema(description = "Statut actuel", example = "PAID")
    String status,
    
    @Schema(description = "Montant total TTC", example = "1500000")
    BigDecimal totalAmount,
    
    @Schema(description = "URL de paiement (si applicable)")
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
