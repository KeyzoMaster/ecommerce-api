package com.lemzo.ecommerce.domain.shipping.repository;

import com.lemzo.ecommerce.domain.shipping.domain.Shipment;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository {

    @Insert
    Shipment insert(Shipment shipment);

    @Update
    Shipment update(Shipment shipment);

    @Find
    Optional<Shipment> findByOrderId(UUID orderId);

    @Find
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
