package com.lemzo.ecommerce.domain.shipping.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant une expédition.
 */
@Entity
@Table(name = "shipping_shipments")
@Getter
@Setter
@NoArgsConstructor
public class Shipment extends AbstractEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShippingStatus status = ShippingStatus.PENDING;

    @Column(nullable = false)
    private String carrier; // ex: Chronopost, DHL

    private LocalDateTime estimatedDeliveryDate;

    public enum ShippingStatus {
        PENDING, SHIPPED, IN_TRANSIT, DELIVERED, RETURNED
    }

    public Shipment(UUID orderId, String trackingNumber, String carrier) {
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
    }
}
