package com.lemzo.ecommerce.domain.marketing.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.marketing.api.dto.CouponCreateRequest;
import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import com.lemzo.ecommerce.domain.marketing.domain.ProductPromotion;
import com.lemzo.ecommerce.domain.marketing.repository.CouponRepository;
import com.lemzo.ecommerce.domain.marketing.repository.ProductPromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarketingService Unit Tests")
class MarketingServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private ProductPromotionRepository promotionRepository;

    @InjectMocks
    private MarketingService marketingService;

    // --- APPLY COUPON ---

    @Test
    @DisplayName("Should apply coupon correctly")
    void shouldApplyCoupon() {
        final String code = "SALE10";
        final Coupon coupon = new Coupon(code, "PERCENTAGE", new BigDecimal("10"));
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
        when(couponRepository.update(any(Coupon.class))).thenAnswer(i -> i.getArgument(0));

        final Optional<BigDecimal> discount = marketingService.applyCoupon(code, new BigDecimal("1000"));

        assertTrue(discount.isPresent());
        assertEquals(new BigDecimal("10"), discount.get());
        assertEquals(1, coupon.getUsageCount());
        verify(couponRepository).update(coupon);
    }

    @Test
    @DisplayName("Should throw BusinessRuleException if coupon not found")
    void shouldThrowIfCouponNotFound() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        final BusinessRuleException ex = assertThrows(BusinessRuleException.class, 
            () -> marketingService.applyCoupon("INVALID", new BigDecimal("1000")));
        assertEquals("Coupon invalide", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException if coupon is invalid (expired)")
    void shouldThrowIfCouponExpired() {
        final String code = "EXPIRED";
        final Coupon coupon = new Coupon(code, "PERCENTAGE", new BigDecimal("10"));
        coupon.setEndDate(OffsetDateTime.now().minusDays(1));
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));

        final BusinessRuleException ex = assertThrows(BusinessRuleException.class, 
            () -> marketingService.applyCoupon(code, new BigDecimal("1000")));
        assertEquals("Coupon expiré ou limite d'utilisation atteinte", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException if order amount is less than min order amount")
    void shouldThrowIfOrderAmountTooLow() {
        final String code = "MIN100";
        final Coupon coupon = new Coupon(code, "PERCENTAGE", new BigDecimal("10"));
        coupon.setMinOrderAmount(new BigDecimal("100"));
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));

        final BusinessRuleException ex = assertThrows(BusinessRuleException.class, 
            () -> marketingService.applyCoupon(code, new BigDecimal("50")));
        assertEquals("Montant minimum de commande non atteint", ex.getMessage());
    }

    // --- CREATE COUPON ---

    @Test
    @DisplayName("Should create coupon successfully")
    void shouldCreateCoupon() {
        final CouponCreateRequest request = new CouponCreateRequest("NEW", "PERCENTAGE", BigDecimal.TEN, BigDecimal.ZERO, null, null, null);
        when(couponRepository.findByCode("NEW")).thenReturn(Optional.empty());
        when(couponRepository.insert(any(Coupon.class))).thenAnswer(i -> i.getArgument(0));

        final Coupon coupon = marketingService.createCoupon(request);

        assertNotNull(coupon);
        assertEquals("NEW", coupon.getCode());
        verify(couponRepository).insert(any(Coupon.class));
    }

    @Test
    @DisplayName("Should fail to create coupon if code already exists")
    void shouldFailIfCodeExists() {
        final CouponCreateRequest request = new CouponCreateRequest("EXISTING", "PERCENTAGE", BigDecimal.TEN, BigDecimal.ZERO, null, null, null);
        when(couponRepository.findByCode("EXISTING")).thenReturn(Optional.of(new Coupon("EXISTING", "P", BigDecimal.ONE)));

        final BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> marketingService.createCoupon(request));
        assertEquals("Le coupon EXISTING existe déjà", ex.getMessage());
        verify(couponRepository, never()).insert(any());
    }

    // --- OTHER METHODS ---

    @Test
    @DisplayName("Should increment usage count of a coupon")
    void shouldIncrementUsage() {
        final String code = "SALE10";
        final Coupon coupon = new Coupon(code, "PERCENTAGE", new BigDecimal("10"));
        coupon.setUsageCount(2);
        
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));

        marketingService.incrementUsage(code);

        assertEquals(3, coupon.getUsageCount());
        verify(couponRepository).update(coupon);
    }

    @Test
    @DisplayName("Should return active product promotion")
    void shouldGetActivePromotion() {
        final UUID productId = UUID.randomUUID();
        final ProductPromotion promo = new ProductPromotion(productId, new BigDecimal("15"), OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));

        when(promotionRepository.findActivePromotion(productId)).thenReturn(Optional.of(promo));

        final Optional<BigDecimal> discount = marketingService.getActivePromotion(productId);

        assertTrue(discount.isPresent());
        assertEquals(new BigDecimal("15"), discount.get());
    }

    @Test
    @DisplayName("Should return empty if promotion is not active")
    void shouldReturnEmptyIfPromotionNotActive() {
        final UUID productId = UUID.randomUUID();
        final ProductPromotion promo = new ProductPromotion(productId, new BigDecimal("15"), null, OffsetDateTime.now().minusDays(1));

        when(promotionRepository.findActivePromotion(productId)).thenReturn(Optional.of(promo));

        final Optional<BigDecimal> discount = marketingService.getActivePromotion(productId);

        assertTrue(discount.isEmpty());
    }
}