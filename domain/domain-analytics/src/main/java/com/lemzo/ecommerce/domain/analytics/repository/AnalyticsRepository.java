package com.lemzo.ecommerce.domain.analytics.repository;

import com.lemzo.ecommerce.domain.analytics.api.dto.DailySalesStats;
import com.lemzo.ecommerce.domain.analytics.api.dto.TopProductStats;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import java.util.List;

@Repository
public interface AnalyticsRepository {

    /**
     * Calcule le chiffre d'affaires quotidien sur les 30 derniers jours.
     * Utilise une CTE pour générer les dates manquantes (PostgreSQL).
     */
    @Query("""
        WITH RECURSIVE date_range AS (
            SELECT CURRENT_DATE - INTERVAL '29 days' as day
            UNION ALL
            SELECT day + INTERVAL '1 day' FROM date_range WHERE day < CURRENT_DATE
        )
        SELECT new com.lemzo.ecommerce.domain.analytics.api.dto.DailySalesStats(
            dr.day,
            COUNT(o.id),
            COALESCE(SUM(o.totalPrice), 0)
        )
        FROM date_range dr
        LEFT JOIN Order o ON CAST(o.createdAt AS date) = dr.day AND o.status != 'CANCELLED'
        GROUP BY dr.day
        ORDER BY dr.day
    """)
    List<DailySalesStats> getDailyTrends();

    /**
     * Identifie les produits les plus vendus.
     * Utilise les Window Functions pour le classement (RANK()).
     */
    @Query("""
        SELECT new com.lemzo.ecommerce.domain.analytics.api.dto.TopProductStats(
            i.productId,
            p.name,
            SUM(i.quantity),
            SUM(i.subtotal),
            CASE WHEN p.viewCount > 0 THEN (CAST(SUM(i.quantity) AS numeric) / p.viewCount) * 100 ELSE 0 END,
            CAST(RANK() OVER (ORDER BY SUM(i.subtotal) DESC) AS int)
        )
        FROM OrderItem i
        JOIN Product p ON p.id = i.productId
        JOIN i.order o
        WHERE o.status != 'CANCELLED'
        GROUP BY i.productId, p.name, p.viewCount
        LIMIT 10
    """)
    List<TopProductStats> getTopProducts();
}
