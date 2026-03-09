package com.lemzo.ecommerce.domain.marketing.api.dto;

import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Réponse pour un coupon.
 */
public record CouponResponse(
    UUID id,
    String code,
    String type,
    BigDecimal value,
    BigDecimal minOrderAmount,
    int usageCount,
    Integer maxUsages,
    OffsetDateTime endDate,
    boolean expired
) {
    public static CouponResponse from(final Coupon coupon) {
        final OffsetDateTime now = OffsetDateTime.now();
        final boolean isExpired = Optional.ofNullable(coupon.getEndDate())
                .map(now::isAfter)
                .orElse(false);
        
        return new CouponResponse(
            coupon.getId(),
            coupon.getCode(),
            coupon.getType(),
            coupon.getValue(),
            coupon.getMinOrderAmount(),
            coupon.getUsageCount(),
            coupon.getMaxUsages(),
            coupon.getEndDate(),
            isExpired
        );
    }
}
