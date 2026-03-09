package com.lemzo.ecommerce.domain.shipping.service;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.core.domain.shipping.ShippingMethod;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultShippingRateProviderTest {

    private DefaultShippingRateProvider provider;
    private Address address;
    private UUID storeId;

    @BeforeEach
    void setUp() {
        provider = new DefaultShippingRateProvider();
        address = Address.builder()
                .label("Maison")
                .street("Street")
                .city("Dakar")
                .zipCode("10000")
                .country("Sénégal")
                .build();
        storeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should calculate standard rate for light product")
    void shouldCalculateStandardRate() {
        var item = new OrderItem(UUID.randomUUID(), storeId, 1, new BigDecimal("1000"), new BigDecimal("0.5"), Map.of());
        
        BigDecimal rate = provider.calculateRate(storeId, address, ShippingMethod.STANDARD, new BigDecimal("1000"), List.of(item));
        
        // Formule actuelle : (2000 base + 0.5 * 500 weight) * 1 zone = 2250
        assertTrue(new BigDecimal("2250").compareTo(rate) == 0);
    }

    @Test
    @DisplayName("Should be free if above threshold")
    void shouldBeFreeAboveThreshold() {
        var item = new OrderItem(UUID.randomUUID(), storeId, 1, new BigDecimal("60000"), new BigDecimal("1.0"), Map.of());
        
        BigDecimal rate = provider.calculateRate(storeId, address, ShippingMethod.STANDARD, new BigDecimal("60000"), List.of(item));
        
        assertTrue(BigDecimal.ZERO.compareTo(rate) == 0);
    }

    @Test
    @DisplayName("Should apply international multiplier")
    void shouldApplyInternationalMultiplier() {
        var internationalAddr = Address.builder()
                .country("France")
                .build();
        var item = new OrderItem(UUID.randomUUID(), storeId, 1, new BigDecimal("1000"), new BigDecimal("0"), Map.of());
        
        BigDecimal rate = provider.calculateRate(storeId, internationalAddr, ShippingMethod.STANDARD, new BigDecimal("1000"), List.of(item));
        
        // (2000 base + 0 weight) * 2.5 zone = 5000
        assertTrue(new BigDecimal("5000").compareTo(rate) == 0);
    }
}
