package com.lemzo.ecommerce.domain.shipping.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant une expédition (Shipment).
 */
@Entity
@Table(name = "shipping_shipments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment extends AbstractEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @Column(nullable = false)
    private String carrier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.PREPARING;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    public Shipment(final UUID orderId, final String trackingNumber, final String carrier) {
        super();
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
    }

    public enum ShipmentStatus {
        PREPARING, SHIPPED, IN_TRANSIT, DELIVERED, RETURNED
    }
}
