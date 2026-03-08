package com.lemzo.ecommerce.domain.marketing.api.dto;

import com.lemzo.ecommerce.domain.marketing.domain.Coupon.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Requête pour la création d'un coupon")
public record CouponCreateRequest(
    @NotBlank @Schema(description = "Code promo unique", example = "SOLDE2025")
    String code,
    
    @NotNull @Schema(description = "Type de remise")
    DiscountType type,
    
    @NotNull @Positive @Schema(description = "Valeur de la remise", example = "15.00")
    BigDecimal value,
    
    @Schema(description = "Date de début")
    LocalDateTime startDate,
    
    @Schema(description = "Date de fin")
    LocalDateTime endDate,
    
    @Schema(description = "Limite d'utilisation totale", example = "100")
    Integer usageLimit
) {}
