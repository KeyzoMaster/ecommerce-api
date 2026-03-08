package com.lemzo.ecommerce.domain.analytics.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Statistiques de performance d'un produit")
public record TopProductStats(
    @Schema(description = "ID du produit")
    UUID productId,
    
    @Schema(description = "Nom du produit")
    String productName,
    
    @Schema(description = "Quantité totale vendue")
    long totalQuantity,
    
    @Schema(description = "Chiffre d'affaires généré")
    BigDecimal totalRevenue,

    @Schema(description = "Taux de conversion (%)")
    BigDecimal conversionRate,
    
    @Schema(description = "Rang de performance (PostgreSQL Window Function)")
    int rank
) {}
