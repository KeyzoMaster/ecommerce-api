package com.lemzo.ecommerce.domain.shipping.api.dto;

import com.lemzo.ecommerce.domain.shipping.domain.Shipment;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse pour une expédition.
 */
@Schema(description = "Informations de suivi d'une expédition")
public record ShipmentResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "ID de la commande associée")
    UUID orderId,
    
    @Schema(description = "Numéro de suivi transporteur", example = "TRK-12345678")
    String trackingNumber,
    
    @Schema(description = "Nom du transporteur", example = "DHL")
    String carrier,
    
    @Schema(description = "Statut de livraison", example = "IN_TRANSIT")
    String status,
    
    @Schema(description = "Date de livraison estimée")
    OffsetDateTime estimatedDeliveryDate
) {
    public static ShipmentResponse from(final Shipment shipment) {
        return new ShipmentResponse(
            shipment.getId(),
            shipment.getOrderId(),
            shipment.getTrackingNumber(),
            shipment.getCarrier(),
            shipment.getStatus().name(),
            shipment.getEstimatedDeliveryDate()
        );
    }
}
