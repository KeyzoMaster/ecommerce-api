package com.lemzo.ecommerce.domain.shipping.api.dto;

import com.lemzo.ecommerce.domain.shipping.domain.Shipment;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Réponse pour une expédition.
 */
public record ShipmentResponse(
    UUID id,
    UUID orderId,
    String trackingNumber,
    String carrier,
    String status,
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
