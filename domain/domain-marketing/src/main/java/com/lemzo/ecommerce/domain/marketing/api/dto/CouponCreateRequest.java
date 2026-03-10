package com.lemzo.ecommerce.domain.marketing.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Requête de création de coupon.
 */
@Schema(description = "Données pour la création d'un nouveau coupon")
public record CouponCreateRequest(
    @Schema(description = "Code unique du coupon", example = "SOLDE2026", required = true)
    @NotBlank(message = "Le code est requis")
    String code,
    
    @Schema(description = "Type de remise (PERCENTAGE, FIXED_AMOUNT)", example = "PERCENTAGE", required = true)
    @NotBlank(message = "Le type est requis")
    String type,
    
    @Schema(description = "Valeur de la remise", example = "20.00", required = true)
    @NotNull(message = "La valeur est requise")
    @Positive
    BigDecimal value,
    
    @Schema(description = "Montant minimum requis", example = "100000")
    @Positive
    BigDecimal minOrderAmount,
    
    @Schema(description = "Nombre maximum d'utilisations", example = "50")
    Integer maxUsages,
    
    @Schema(description = "Date de début de validité")
    OffsetDateTime startDate,
    
    @Schema(description = "Date de fin de validité")
    OffsetDateTime endDate
) {}
