package com.lemzo.ecommerce.domain.marketing.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.marketing.api.dto.CouponCreateRequest;
import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import com.lemzo.ecommerce.domain.marketing.repository.CouponRepository;
import com.lemzo.ecommerce.domain.marketing.repository.ProductPromotionRepository;
import com.lemzo.ecommerce.core.annotation.Audit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service de gestion du marketing (coupons, promotions).
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class MarketingService {

    private final CouponRepository couponRepository;
    private final ProductPromotionRepository promotionRepository;

    @Transactional
    @Audit(action = "COUPON_CREATE")
    public Coupon createCoupon(final CouponCreateRequest request) {
        if (couponRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessRuleException("error.marketing.coupon_code_exists");
        }

        final var coupon = new Coupon(request.code(), request.type(), request.value());
        coupon.setMinOrderAmount(Optional.ofNullable(request.minOrderAmount()).orElse(BigDecimal.ZERO));
        coupon.setMaxUsages(request.maxUsages());
        coupon.setStartDate(request.startDate());
        coupon.setEndDate(request.endDate());

        return couponRepository.save(coupon);
    }

    public Optional<BigDecimal> applyCoupon(final String code, final BigDecimal currentAmount) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        return couponRepository.findByCode(code)
                .filter(Coupon::isValid)
                .filter(c -> currentAmount.compareTo(c.getMinOrderAmount()) >= 0)
                .map(c -> calculateDiscount(c, currentAmount));
    }

    private BigDecimal calculateDiscount(final Coupon coupon, final BigDecimal amount) {
        return "PERCENTAGE".equals(coupon.getType()) 
                ? amount.multiply(coupon.getValue().divide(new BigDecimal("100")))
                : coupon.getValue();
    }

    @Transactional
    public void incrementUsage(final String code) {
        couponRepository.findByCode(code)
                .ifPresent(c -> {
                    c.setUsageCount(c.getUsageCount() + 1);
                    couponRepository.save(c);
                });
    }
}
