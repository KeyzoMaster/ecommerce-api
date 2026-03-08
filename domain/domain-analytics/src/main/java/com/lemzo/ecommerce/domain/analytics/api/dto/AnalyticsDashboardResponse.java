package com.lemzo.ecommerce.domain.analytics.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.List;

@Schema(description = "Données consolidées du tableau de bord analytics")
public record AnalyticsDashboardResponse(
    @Schema(description = "Évolution des ventes sur les 30 derniers jours")
    List<DailySalesStats> dailyTrends,
    
    @Schema(description = "Top 10 des produits les plus vendus")
    List<TopProductStats> topProducts
) {}
