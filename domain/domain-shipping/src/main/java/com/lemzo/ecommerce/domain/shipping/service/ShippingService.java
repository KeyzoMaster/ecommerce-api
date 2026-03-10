package com.lemzo.ecommerce.domain.shipping.service;

import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.domain.shipping.domain.Shipment;
import com.lemzo.ecommerce.domain.shipping.repository.ShipmentRepository;
import com.lemzo.ecommerce.core.annotation.Audit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service de gestion des expéditions.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ShippingService {

    private final ShipmentRepository shipmentRepository;

    @Transactional
    @Audit(action = "SHIPMENT_CREATE")
    public Shipment createShipment(final UUID orderId, final String carrier) {
        final var trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        final var shipment = new Shipment(orderId, trackingNumber, carrier);
        shipment.setEstimatedDeliveryDate(OffsetDateTime.now().plusDays(3));
        return shipmentRepository.insert(shipment);
    }

    @Transactional
    @Audit(action = "SHIPMENT_STATUS_UPDATE")
    public Shipment updateStatus(final String trackingNumber, final Shipment.ShipmentStatus status) {
        final var shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Expédition non trouvée"));
        
        shipment.setStatus(status);
        return shipmentRepository.update(shipment);
    }

    public Optional<Shipment> findShipmentByOrderId(final UUID orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }
}
