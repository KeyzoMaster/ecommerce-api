package com.lemzo.ecommerce.domain.analytics.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Statistiques pour un produit phare.
 */
@Schema(description = "Indicateurs de vente pour un produit spécifique")
public record TopProductStats(
    @Schema(description = "Identifiant du produit")
    UUID productId,
    
    @Schema(description = "Nom du produit", example = "MacBook Pro")
    String productName,
    
    @Schema(description = "Quantité totale vendue", example = "150")
    long totalSold,
    
    @Schema(description = "Chiffre d'affaires total généré", example = "225000000.00")
    BigDecimal totalRevenue
) {}
