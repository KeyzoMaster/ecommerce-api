package com.lemzo.ecommerce.domain.marketing.api.dto;

import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse pour un coupon.
 */
@Schema(description = "Informations sur un coupon de réduction")
public record CouponResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Code promotionnel", example = "PROMO2026")
    String code,
    
    @Schema(description = "Type de remise", example = "PERCENTAGE")
    String type,
    
    @Schema(description = "Valeur de la remise", example = "15.00")
    BigDecimal value,
    
    @Schema(description = "Montant minimum de commande requis", example = "50000")
    BigDecimal minOrderAmount,
    
    @Schema(description = "Nombre d'utilisations actuelles", example = "10")
    int usageCount,
    
    @Schema(description = "Nombre maximum d'utilisations (si limité)", example = "100")
    Integer maxUsages,
    
    @Schema(description = "Date de fin de validité")
    OffsetDateTime endDate,
    
    @Schema(description = "Indique si le coupon a expiré")
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
