package com.lemzo.ecommerce.domain.analytics.service;

import com.lemzo.ecommerce.core.contract.util.CsvExportPort;
import com.lemzo.ecommerce.domain.analytics.api.dto.DailySalesStats;
import com.lemzo.ecommerce.domain.analytics.api.dto.AnalyticsDashboardResponse;
import com.lemzo.ecommerce.domain.analytics.api.dto.TopProductStats;
import com.lemzo.ecommerce.domain.analytics.repository.AnalyticsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service pour les statistiques et analyses.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final CsvExportPort csvExportPort;

    public AnalyticsDashboardResponse getDashboard() {
        final List<DailySalesStats> dailyTrends = analyticsRepository.getDailySales();
        final List<TopProductStats> topProducts = analyticsRepository.getTopProducts();

        final BigDecimal totalRevenue = dailyTrends.stream()
                .map(DailySalesStats::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final long totalOrders = dailyTrends.stream()
                .mapToLong(DailySalesStats::count)
                .sum();

        return new AnalyticsDashboardResponse(totalRevenue, totalOrders, dailyTrends, topProducts);
    }

    public String exportDailyTrendsCsv() {
        final List<DailySalesStats> data = analyticsRepository.getDailySales();
        final List<String> headers = List.of("Date", "Commandes", "Revenu");

        return csvExportPort.generateCsv(headers, data, stats -> List.of(
                stats.date().toString(),
                String.valueOf(stats.count()),
                stats.revenue().toString()
        ));
    }

    public String exportTopProductsCsv() {
        final List<TopProductStats> data = analyticsRepository.getTopProducts();
        final List<String> headers = List.of("Produit ID", "Nom", "Ventes", "Revenu");

        return csvExportPort.generateCsv(headers, data, stats -> List.of(
                stats.productId().toString(),
                stats.productName(),
                String.valueOf(stats.totalSold()),
                stats.totalRevenue().toString()
        ));
    }
}
