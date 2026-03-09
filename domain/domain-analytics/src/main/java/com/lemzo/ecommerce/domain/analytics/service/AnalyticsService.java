package com.lemzo.ecommerce.domain.analytics.service;

import com.lemzo.ecommerce.domain.analytics.api.dto.AnalyticsDashboardResponse;
import com.lemzo.ecommerce.domain.analytics.api.dto.DailySalesStats;
import com.lemzo.ecommerce.domain.analytics.api.dto.TopProductStats;
import com.lemzo.ecommerce.domain.analytics.repository.AnalyticsRepository;
import com.lemzo.ecommerce.util.document.CsvExportUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la génération de statistiques et rapports.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final CsvExportUtil csvExportUtil;

    public AnalyticsDashboardResponse getDashboard() {
        final var dailyTrends = analyticsRepository.getDailySales();
        final var topProducts = analyticsRepository.getTopProducts().stream()
                .limit(5)
                .collect(Collectors.toList());
        
        final var totalRevenue = dailyTrends.stream()
                .map(DailySalesStats::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        final long totalOrders = dailyTrends.stream()
                .mapToLong(DailySalesStats::count)
                .sum();

        return new AnalyticsDashboardResponse(totalRevenue, totalOrders, topProducts, dailyTrends);
    }

    public String exportTopProductsCsv() {
        final var topProducts = analyticsRepository.getTopProducts();
        final var headers = List.of("ID Produit", "Nom", "Quantité Vendue", "Chiffre d'Affaires");
        
        return csvExportUtil.generateCsv(headers, topProducts, p -> List.of(
                p.productId().toString(),
                p.productName(),
                String.valueOf(p.totalSold()),
                p.totalRevenue().toString()
        ));
    }

    public String exportDailyTrendsCsv() {
        final var trends = analyticsRepository.getDailySales();
        final var headers = List.of("Date", "Nombre de Commandes", "Revenu");
        
        return csvExportUtil.generateCsv(headers, trends, s -> List.of(
                s.date().toString(),
                String.valueOf(s.count()),
                s.revenue().toString()
        ));
    }
}
