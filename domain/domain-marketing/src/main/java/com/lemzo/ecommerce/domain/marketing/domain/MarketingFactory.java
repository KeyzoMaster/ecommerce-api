package com.lemzo.ecommerce.domain.marketing.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Factory pour les entités du domaine Marketing.
 */
public final class MarketingFactory {

    private MarketingFactory() {
        // Classe utilitaire
    }

    /**
     * Crée un coupon de réduction.
     */
    public static Coupon createCoupon(final String code, final String type, final BigDecimal value) {
        final Coupon coupon = new Coupon(code, type, value);
        coupon.setUsageCount(0);
        coupon.setMinOrderAmount(BigDecimal.ZERO);
        coupon.setStartDate(LocalDateTime.now());
        coupon.setEndDate(LocalDateTime.now().plusMonths(1));
        return coupon;
    }
}
