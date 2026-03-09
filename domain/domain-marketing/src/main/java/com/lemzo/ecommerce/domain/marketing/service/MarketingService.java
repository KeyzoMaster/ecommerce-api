package com.lemzo.ecommerce.domain.marketing.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.core.marketing.MarketingPort;
import com.lemzo.ecommerce.domain.marketing.api.dto.CouponCreateRequest;
import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import com.lemzo.ecommerce.domain.marketing.domain.ProductPromotion;
import com.lemzo.ecommerce.domain.marketing.repository.CouponRepository;
import com.lemzo.ecommerce.domain.marketing.repository.ProductPromotionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des coupons et promotions.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class MarketingService implements MarketingPort {

    private final CouponRepository couponRepository;
    private final ProductPromotionRepository promotionRepository;

    @Override
    @Transactional
    public Optional<BigDecimal> applyCoupon(final String code, final BigDecimal orderAmount) {
        final Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new BusinessRuleException("Coupon invalide"));

        if (!coupon.isValid()) {
            throw new BusinessRuleException("Coupon expiré ou limite d'utilisation atteinte");
        }

        if (orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BusinessRuleException("Montant minimum de commande non atteint");
        }

        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.update(coupon);

        return Optional.of(coupon.getValue());
    }

    @Transactional
    public Coupon createCoupon(final CouponCreateRequest request) {
        if (couponRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessRuleException("Le coupon " + request.code() + " existe déjà");
        }
        
        final Coupon coupon = new Coupon(request.code(), request.type(), request.value());
        coupon.setMinOrderAmount(Optional.ofNullable(request.minOrderAmount()).orElse(BigDecimal.ZERO));
        coupon.setMaxUsages(request.maxUsages());
        coupon.setStartDate(request.startDate());
        coupon.setEndDate(request.endDate());
        
        return couponRepository.insert(coupon);
    }

    public Optional<BigDecimal> getActivePromotion(final UUID productId) {
        return promotionRepository.findActivePromotion(productId)
                .filter(ProductPromotion::isActive)
                .map(ProductPromotion::getDiscountValue);
    }

    public Optional<Coupon> findCouponByCode(final String code) {
        return couponRepository.findByCode(code);
    }

    @Override
    @Transactional
    public void incrementUsage(final String code) {
        couponRepository.findByCode(code)
                .ifPresent(c -> {
                    c.setUsageCount(c.getUsageCount() + 1);
                    couponRepository.update(c);
                });
    }
}
