package com.lemzo.ecommerce.domain.marketing.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import com.lemzo.ecommerce.domain.marketing.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketingServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private MarketingService marketingService;

    private Coupon fixedCoupon;
    private Coupon percentCoupon;

    @BeforeEach
    void setUp() {
        fixedCoupon = new Coupon("FIXED10", Coupon.DiscountType.FIXED_AMOUNT, new BigDecimal("1000"));
        fixedCoupon.setActive(true);
        
        percentCoupon = new Coupon("PERCENT10", Coupon.DiscountType.PERCENTAGE, new BigDecimal("10"));
        percentCoupon.setActive(true);
    }

    @Test
    void validateAndApplyCoupon_FixedAmount_ShouldWork() {
        when(couponRepository.findByCode("FIXED10")).thenReturn(Optional.of(fixedCoupon));
        
        BigDecimal discount = marketingService.validateAndApplyCoupon("FIXED10", new BigDecimal("5000"));
        
        assertEquals(new BigDecimal("1000"), discount);
        assertEquals(1, fixedCoupon.getUsedCount());
        verify(couponRepository).save(fixedCoupon);
    }

    @Test
    void validateAndApplyCoupon_Percentage_ShouldWork() {
        when(couponRepository.findByCode("PERCENT10")).thenReturn(Optional.of(percentCoupon));
        
        BigDecimal discount = marketingService.validateAndApplyCoupon("PERCENT10", new BigDecimal("5000"));
        
        assertEquals(new BigDecimal("500.00"), discount);
        assertEquals(1, percentCoupon.getUsedCount());
    }

    @Test
    void validateAndApplyCoupon_Expired_ShouldThrowException() {
        fixedCoupon.setActive(false);
        when(couponRepository.findByCode("FIXED10")).thenReturn(Optional.of(fixedCoupon));
        
        assertThrows(BusinessRuleException.class, () -> 
            marketingService.validateAndApplyCoupon("FIXED10", new BigDecimal("5000"))
        );
    }

    @Test
    void validateAndApplyCoupon_NotFound_ShouldThrowException() {
        when(couponRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());
        
        assertThrows(BusinessRuleException.class, () -> 
            marketingService.validateAndApplyCoupon("UNKNOWN", new BigDecimal("5000"))
        );
    }
}
