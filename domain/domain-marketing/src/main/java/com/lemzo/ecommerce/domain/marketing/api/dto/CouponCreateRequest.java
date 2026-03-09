package com.lemzo.ecommerce.domain.marketing.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Requête de création de coupon.
 */
public record CouponCreateRequest(
    @NotBlank(message = "Le code est requis")
    String code,
    
    @NotBlank(message = "Le type est requis")
    String type, // PERCENTAGE, FIXED_AMOUNT
    
    @NotNull(message = "La valeur est requise")
    @Positive
    BigDecimal value,
    
    @Positive
    BigDecimal minOrderAmount,
    
    Integer maxUsages,
    
    OffsetDateTime startDate,
    
    OffsetDateTime endDate
) {}
