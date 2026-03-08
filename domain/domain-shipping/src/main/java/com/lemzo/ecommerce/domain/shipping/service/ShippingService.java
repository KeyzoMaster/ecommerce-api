package com.lemzo.ecommerce.domain.shipping.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.domain.shipping.domain.Shipment;
import com.lemzo.ecommerce.domain.shipping.repository.ShipmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
public class ShippingService {

    @Inject
    private ShipmentRepository shipmentRepository;

    @Transactional
    @Audit(action = "SHIPMENT_CREATE")
    public Shipment initiateShipment(UUID orderId, String carrier) {
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Shipment shipment = new Shipment(orderId, trackingNumber, carrier);
        return shipmentRepository.insert(shipment);
    }

    @Transactional
    @Audit(action = "SHIPMENT_UPDATE_STATUS")
    public Shipment updateStatus(String trackingNumber, Shipment.ShippingStatus status) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .map(shipment -> {
                    shipment.setStatus(status);
                    return shipmentRepository.save(shipment);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Expédition non trouvée : " + trackingNumber));
    }
}
