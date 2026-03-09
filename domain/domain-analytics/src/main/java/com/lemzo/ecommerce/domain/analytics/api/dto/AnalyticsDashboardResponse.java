package com.lemzo.ecommerce.domain.analytics.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Réponse globale pour le tableau de bord analytics.
 */
public record AnalyticsDashboardResponse(
    BigDecimal totalRevenue,
    long totalOrders,
    List<DailySalesStats> dailyTrends,
    List<TopProductStats> topProducts
) {}
