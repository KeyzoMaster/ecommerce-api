package com.lemzo.ecommerce.domain.sales.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Information sur un mode de livraison")
public record ShippingMethodResponse(
    @Schema(description = "Code du mode de livraison", example = "EXPRESS")
    String code,
    
    @Schema(description = "Nom affiché", example = "Livraison Express (24h)")
    String label,
    
    @Schema(description = "Coût fixe", example = "5000")
    BigDecimal cost,
    
    @Schema(description = "Délai estimé (jours)", example = "1")
    int estimatedDays
) {}
