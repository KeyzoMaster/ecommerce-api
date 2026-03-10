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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

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
        final var dailyTrends = getDailySales();
        final var topProducts = getTopProducts();

        final var totalRevenue = dailyTrends.stream()
                .map(DailySalesStats::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var totalOrders = dailyTrends.stream()
                .mapToLong(DailySalesStats::count)
                .sum();

        return new AnalyticsDashboardResponse(totalRevenue, totalOrders, dailyTrends, topProducts);
    }

    private List<DailySalesStats> getDailySales() {
        return Optional.ofNullable(analyticsRepository.getDailySalesRaw())
                .map(rows -> rows.stream()
                        .map(this::mapToDailySalesStats)
                        .toList())
                .orElseGet(List::of);
    }

    private DailySalesStats mapToDailySalesStats(final Object[] row) {
        final OffsetDateTime date = switch (row[0]) {
            case OffsetDateTime odt -> odt;
            case Timestamp ts -> ts.toInstant().atOffset(ZoneOffset.UTC);
            default -> throw new IllegalArgumentException("Type de date non supporté : " + row[0].getClass());
        };
        return new DailySalesStats(
            date,
            ((Number) row[1]).longValue(),
            (BigDecimal) row[2]
        );
    }

    private List<TopProductStats> getTopProducts() {
        final var raw = Optional.ofNullable(analyticsRepository.getTopProductsRaw()).orElseGet(List::of);
        final Map<UUID, TopProductStats> aggregator = new HashMap<>();
        
        raw.forEach(row -> {
            final var qty = ((Number) row[0]).longValue();
            final var price = (BigDecimal) row[1];
            final var id = (UUID) row[2];
            final var name = (String) row[3];
            final var revenue = price.multiply(BigDecimal.valueOf(qty));
            
            aggregator.merge(id, 
                new TopProductStats(id, name, qty, revenue),
                (old, newVal) -> new TopProductStats(id, name, 
                    old.totalSold() + newVal.totalSold(), 
                    old.totalRevenue().add(newVal.totalRevenue()))
            );
        });

        return aggregator.values().stream()
                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                .limit(10)
                .toList();
    }

    public String exportDailyTrendsCsv() {
        final var data = getDailySales();
        final var headers = List.of("Date", "Commandes", "Revenu");

        return csvExportPort.generateCsv(headers, data, stats -> List.of(
                stats.date().toString(),
                String.valueOf(stats.count()),
                stats.revenue().toString()
        ));
    }

    public String exportTopProductsCsv() {
        final var data = getTopProducts();
        final var headers = List.of("Produit ID", "Nom", "Ventes", "Revenu");

        return csvExportPort.generateCsv(headers, data, stats -> List.of(
                stats.productId().toString(),
                stats.productName(),
                String.valueOf(stats.totalSold()),
                stats.totalRevenue().toString()
        ));
    }
}
