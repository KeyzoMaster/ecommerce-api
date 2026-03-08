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
        address = new Address("Street", "Dakar", "10000", "Sénégal");
        storeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should calculate standard rate for light product")
    void shouldCalculateStandardRate() {
        OrderItem item = new OrderItem(UUID.randomUUID(), 1, new BigDecimal("1000"), new BigDecimal("0.5"), Map.of());
        
        BigDecimal rate = provider.calculateRate(storeId, address, ShippingMethod.STANDARD, new BigDecimal("1000"), List.of(item));
        
        // 2000 (standard base) + 0.5 * 500 (weight) = 2250
        assertTrue(new BigDecimal("2250").compareTo(rate) == 0);
    }

    @Test
    @DisplayName("Should be free if above threshold")
    void shouldBeFreeAboveThreshold() {
        Map<String, Object> config = Map.of("free_over", 50000);
        OrderItem item = new OrderItem(UUID.randomUUID(), 1, new BigDecimal("60000"), new BigDecimal("1.0"), config);
        
        BigDecimal rate = provider.calculateRate(storeId, address, ShippingMethod.STANDARD, new BigDecimal("60000"), List.of(item));
        
        assertTrue(BigDecimal.ZERO.compareTo(rate) == 0);
    }

    @Test
    @DisplayName("Should apply heavy penalty if method not allowed")
    void shouldApplyPenaltyIfNotAllowed() {
        Map<String, Object> config = Map.of("allowed_methods", List.of("EXPRESS"));
        OrderItem item = new OrderItem(UUID.randomUUID(), 1, new BigDecimal("1000"), new BigDecimal("1.0"), config);
        
        BigDecimal rate = provider.calculateRate(storeId, address, ShippingMethod.STANDARD, new BigDecimal("1000"), List.of(item));
        
        assertTrue(new BigDecimal("10000").compareTo(rate) == 0);
    }

    @Test
    @DisplayName("Should use custom base rates from config")
    void shouldUseCaseBaseRates() {
        Map<String, Object> baseRates = Map.of("EXPRESS", 3500);
        Map<String, Object> config = Map.of("base_rates", baseRates);
        OrderItem item = new OrderItem(UUID.randomUUID(), 2, new BigDecimal("1000"), new BigDecimal("0"), config);
        
        BigDecimal rate = provider.calculateRate(storeId, address, ShippingMethod.EXPRESS, new BigDecimal("2000"), List.of(item));
        
        // (3500 base + 0 weight) * 2 quantity = 7000
        assertTrue(new BigDecimal("7000").compareTo(rate) == 0);
    }
}
