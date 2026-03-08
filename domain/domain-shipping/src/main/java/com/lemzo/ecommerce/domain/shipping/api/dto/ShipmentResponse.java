package com.lemzo.ecommerce.domain.shipping.api.dto;

import com.lemzo.ecommerce.domain.shipping.domain.Shipment;
import com.lemzo.ecommerce.domain.shipping.domain.Shipment.ShippingStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Informations sur l'expédition")
public record ShipmentResponse(
    UUID id,
    UUID orderId,
    @Schema(description = "Numéro de suivi", example = "TRK-ABCD123")
    String trackingNumber,
    ShippingStatus status,
    @Schema(description = "Transporteur", example = "DHL")
    String carrier,
    LocalDateTime estimatedDeliveryDate
) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
            shipment.getId(),
            shipment.getOrderId(),
            shipment.getTrackingNumber(),
            shipment.getStatus(),
            shipment.getCarrier(),
            shipment.getEstimatedDeliveryDate()
        );
    }
}
