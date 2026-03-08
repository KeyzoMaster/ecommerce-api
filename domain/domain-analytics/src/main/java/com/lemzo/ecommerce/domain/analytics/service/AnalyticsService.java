package com.lemzo.ecommerce.domain.analytics.service;

import com.lemzo.ecommerce.domain.analytics.api.dto.AnalyticsDashboardResponse;
import com.lemzo.ecommerce.domain.analytics.repository.AnalyticsRepository;
import com.lemzo.ecommerce.util.document.CsvExportUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Service pour la récupération des rapports analytics.
 */
@ApplicationScoped
public class AnalyticsService {

    @Inject
    private AnalyticsRepository analyticsRepository;

    @Inject
    private CsvExportUtil csvExportUtil;

    /**
     * Compile les données pour le dashboard global.
     */
    public AnalyticsDashboardResponse getDashboardStats() {
        var dailyTrends = analyticsRepository.getDailyTrends();
        var topProducts = analyticsRepository.getTopProducts();
        
        return new AnalyticsDashboardResponse(dailyTrends, topProducts);
    }

    /**
     * Exporte le top des produits au format CSV.
     */
    public String exportTopProductsToCsv() {
        var topProducts = analyticsRepository.getTopProducts();
        List<String> headers = List.of("Rang", "Produit", "Quantité", "Chiffre d'Affaires", "Taux Conversion (%)");
        
        return csvExportUtil.generateCsv(headers, topProducts, product -> List.of(
                String.valueOf(product.rank()),
                product.productName(),
                String.valueOf(product.totalQuantity()),
                product.totalRevenue().toString(),
                product.conversionRate().toString()
        ));
    }

    /**
     * Exporte les tendances quotidiennes au format CSV.
     */
    public String exportDailyTrendsToCsv() {
        var trends = analyticsRepository.getDailyTrends();
        List<String> headers = List.of("Date", "Nombre de Commandes", "Chiffre d'Affaires");
        
        return csvExportUtil.generateCsv(headers, trends, trend -> List.of(
                trend.date().toString(),
                String.valueOf(trend.orderCount()),
                trend.revenue().toString()
        ));
    }
}
