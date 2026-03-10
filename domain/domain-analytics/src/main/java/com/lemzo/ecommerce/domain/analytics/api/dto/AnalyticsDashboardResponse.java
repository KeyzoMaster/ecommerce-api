package com.lemzo.ecommerce.domain.analytics.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Réponse globale pour le tableau de bord analytics.
 */
@Schema(description = "Données consolidées du tableau de bord d'analyse")
public record AnalyticsDashboardResponse(
    @Schema(description = "Chiffre d'affaires total", example = "5000000")
    BigDecimal totalRevenue,
    
    @Schema(description = "Nombre total de commandes passées", example = "350")
    long totalOrders,
    
    @Schema(description = "Tendances quotidiennes des ventes")
    List<DailySalesStats> dailyTrends,
    
    @Schema(description = "Top 10 des produits les plus performants")
    List<TopProductStats> topProducts
) {}
