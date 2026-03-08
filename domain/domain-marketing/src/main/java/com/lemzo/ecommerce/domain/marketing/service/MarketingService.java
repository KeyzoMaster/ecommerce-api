package com.lemzo.ecommerce.domain.marketing.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.marketing.api.dto.CouponCreateRequest;
import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import com.lemzo.ecommerce.domain.marketing.repository.CouponRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;

/**
 * Service de gestion du marketing.
 */
@ApplicationScoped
public class MarketingService {

    @Inject
    private CouponRepository couponRepository;

    /**
     * Crée un nouveau coupon.
     */
    @Transactional
    @Audit(action = "COUPON_CREATE")
    public Coupon createCoupon(CouponCreateRequest request) {
        if (couponRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessRuleException("error.marketing.coupon_already_exists");
        }

        Coupon coupon = new Coupon(request.code(), request.type(), request.value());
        coupon.setStartDate(request.startDate());
        coupon.setEndDate(request.endDate());
        coupon.setUsageLimit(request.usageLimit());

        return couponRepository.insert(coupon);
    }

    /**
     * Valide un coupon et retourne le montant de la remise.
     */
    @Transactional
    public BigDecimal validateAndApplyCoupon(String code, BigDecimal currentAmount) {
        if (code == null || code.isBlank()) return BigDecimal.ZERO;

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new BusinessRuleException("error.marketing.coupon_not_found"));

        if (!coupon.isValid()) {
            throw new BusinessRuleException("error.marketing.coupon_invalid_or_expired");
        }

        BigDecimal discount = switch (coupon.getType()) {
            case FIXED_AMOUNT -> coupon.getValue();
            case PERCENTAGE -> currentAmount.multiply(coupon.getValue()).divide(new BigDecimal("100"));
        };

        // Incrémenter l'usage
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        return discount.min(currentAmount); // On ne peut pas avoir une remise > au montant
    }
}
