package com.lemzo.ecommerce.domain.shipping.service;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.domain.core.shipping.ShippingMethod;
import com.lemzo.ecommerce.domain.core.sales.OrderLineInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests unitaires pour DefaultShippingRateProvider.
 */
@DisplayName("DefaultShippingRateProvider Unit Tests")
class DefaultShippingRateProviderTest {

    private final DefaultShippingRateProvider rateProvider = new DefaultShippingRateProvider();

    @Test
    @DisplayName("Should return zero if total is above free threshold")
    void shouldReturnZeroForHighTotal() {
        // Arrange
        final var address = Address.builder().country("Sénégal").build();
        final List<OrderLineInfo> items = List.of();
        final var total = new BigDecimal("60000"); // > 50000

        // Act
        final var rate = rateProvider.calculateRate(address, ShippingMethod.STANDARD, total, items);

        // Assert
        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    @DisplayName("Should return standard rate for local shipping")
    void shouldReturnStandardLocalRate() {
        // Arrange
        final var address = Address.builder().country("Sénégal").build();
        final List<OrderLineInfo> items = List.of();
        final var total = new BigDecimal("10000");

        // Act
        final var rate = rateProvider.calculateRate(address, ShippingMethod.STANDARD, total, items);

        // Assert
        assertEquals(new BigDecimal("2000"), rate);
    }

    @Test
    @DisplayName("Should return express rate for local shipping")
    void shouldReturnExpressLocalRate() {
        // Arrange
        final var address = Address.builder().country("Sénégal").build();
        final List<OrderLineInfo> items = List.of();
        final var total = new BigDecimal("10000");

        // Act
        final var rate = rateProvider.calculateRate(address, ShippingMethod.EXPRESS, total, items);

        // Assert
        assertEquals(new BigDecimal("5000"), rate);
    }
}
