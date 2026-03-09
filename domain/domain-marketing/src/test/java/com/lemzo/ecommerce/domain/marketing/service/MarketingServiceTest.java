package com.lemzo.ecommerce.domain.marketing.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.marketing.api.dto.CouponCreateRequest;
import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import com.lemzo.ecommerce.domain.marketing.repository.CouponRepository;
import com.lemzo.ecommerce.domain.marketing.repository.ProductPromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MarketingService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MarketingService Unit Tests")
class MarketingServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private ProductPromotionRepository promotionRepository;

    @InjectMocks
    private MarketingService marketingService;

    @Test
    @DisplayName("Should apply percentage coupon correctly")
    void shouldApplyPercentageCoupon() {
        // Arrange
        final String code = "SALE10";
        final var coupon = new Coupon(code, "PERCENTAGE", new BigDecimal("10"));
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));

        // Act
        final var discount = marketingService.applyCoupon(code, new BigDecimal("1000"));

        // Assert
        assertTrue(discount.isPresent());
        assertEquals(new BigDecimal("100.00"), discount.get());
    }

    @Test
    @DisplayName("Should apply fixed amount coupon correctly")
    void shouldApplyFixedAmountCoupon() {
        // Arrange
        final String code = "FIXED50";
        final var coupon = new Coupon(code, "FIXED_AMOUNT", new BigDecimal("50"));
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));

        // Act
        final var discount = marketingService.applyCoupon(code, new BigDecimal("1000"));

        // Assert
        assertTrue(discount.isPresent());
        assertEquals(new BigDecimal("50"), discount.get());
    }

    @Test
    @DisplayName("Should return empty if coupon is invalid or not found")
    void shouldReturnEmptyForInvalidCoupon() {
        // Arrange
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act
        final var result = marketingService.applyCoupon("INVALID", new BigDecimal("1000"));

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should fail to create coupon if code already exists")
    void shouldFailIfCodeExists() {
        // Arrange
        final var request = new CouponCreateRequest("EXISTING", "PERCENTAGE", BigDecimal.TEN, BigDecimal.ZERO, null, null, null);
        when(couponRepository.findByCode("EXISTING")).thenReturn(Optional.of(new Coupon("EXISTING", "P", BigDecimal.ONE)));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> marketingService.createCoupon(request));
    }
}
