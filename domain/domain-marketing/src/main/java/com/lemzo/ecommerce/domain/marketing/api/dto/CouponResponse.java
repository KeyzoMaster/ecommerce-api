package com.lemzo.ecommerce.domain.marketing.api.dto;

import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import com.lemzo.ecommerce.domain.marketing.domain.Coupon.DiscountType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Détails d'un coupon de réduction")
public record CouponResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Code promo", example = "SOLDE2025")
    String code,
    
    @Schema(description = "Type de remise")
    DiscountType type,
    
    @Schema(description = "Valeur de la remise", example = "10.00")
    BigDecimal value,
    
    @Schema(description = "Date de fin de validité")
    LocalDateTime endDate,
    
    @Schema(description = "Indique si le coupon est expiré")
    boolean expired,
    
    @Schema(description = "Indique si le coupon est actif")
    boolean active
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
            coupon.getId(),
            coupon.getCode(),
            coupon.getType(),
            coupon.getValue(),
            coupon.getEndDate(),
            coupon.isExpired(),
            coupon.isActive()
        );
    }
}
