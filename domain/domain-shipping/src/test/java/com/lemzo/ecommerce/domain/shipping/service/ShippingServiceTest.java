package com.lemzo.ecommerce.domain.shipping.service;

import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.domain.shipping.domain.Shipment;
import com.lemzo.ecommerce.domain.shipping.repository.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ShippingService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingService Unit Tests")
class ShippingServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private ShippingService shippingService;

    @Test
    @DisplayName("Should create shipment successfully")
    void shouldCreateShipmentSuccessfully() {
        // Arrange
        final UUID orderId = UUID.randomUUID();
        when(shipmentRepository.insert(any(Shipment.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        final var result = shippingService.createShipment(orderId, "DHL");

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals("DHL", result.getCarrier());
        assertTrue(result.getTrackingNumber().startsWith("TRK-"));
        verify(shipmentRepository).insert(any(Shipment.class));
    }

    @Test
    @DisplayName("Should update shipment status")
    void shouldUpdateStatus() {
        // Arrange
        final String trk = "TRK-123";
        final var shipment = new Shipment(UUID.randomUUID(), trk, "DHL");
        when(shipmentRepository.findByTrackingNumber(trk)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.update(any(Shipment.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        final var result = shippingService.updateStatus(trk, Shipment.ShipmentStatus.SHIPPED);

        // Assert
        assertEquals(Shipment.ShipmentStatus.SHIPPED, result.getStatus());
        verify(shipmentRepository).update(any(Shipment.class));
    }

    @Test
    @DisplayName("Should throw exception if shipment not found")
    void shouldFailOnNotFound() {
        when(shipmentRepository.findByTrackingNumber(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> shippingService.updateStatus("MISSING", Shipment.ShipmentStatus.DELIVERED));
    }
}
