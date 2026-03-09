package com.lemzo.ecommerce.domain.analytics.repository;

import com.lemzo.ecommerce.domain.analytics.api.dto.DailySalesStats;
import com.lemzo.ecommerce.domain.analytics.api.dto.TopProductStats;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import java.util.List;

@Repository
public interface AnalyticsRepository {

    /**
     * Calcule le chiffre d'affaires quotidien sur les 30 derniers jours.
     */
    @Query("""
        SELECT new com.lemzo.ecommerce.domain.analytics.api.dto.DailySalesStats(
            CAST(o.createdAt AS date),
            COUNT(o.id),
            SUM(o.totalAmount)
        )
        FROM Order o
        WHERE o.status != 'CANCELLED'
        GROUP BY CAST(o.createdAt AS date)
        ORDER BY CAST(o.createdAt AS date) DESC
    """)
    List<DailySalesStats> getDailySales();

    /**
     * Identifie les produits les plus vendus.
     */
    @Query("""
        SELECT new com.lemzo.ecommerce.domain.analytics.api.dto.TopProductStats(
            i.productId,
            p.name,
            SUM(i.quantity),
            SUM(i.subtotal)
        )
        FROM OrderItem i
        JOIN Product p ON p.id = i.productId
        JOIN i.order o
        WHERE o.status != 'CANCELLED'
        GROUP BY i.productId, p.name
        ORDER BY SUM(i.subtotal) DESC
    """)
    List<TopProductStats> getTopProducts();
}
