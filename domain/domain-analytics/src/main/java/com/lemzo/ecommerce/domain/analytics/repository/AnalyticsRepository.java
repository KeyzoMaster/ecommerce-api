package com.lemzo.ecommerce.domain.analytics.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * Implémentation personnalisée pour éviter les bugs de parsing de Jakarta Data Query.
 */
@ApplicationScoped
public class AnalyticsRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Récupère les données brutes pour les ventes quotidiennes (Native SQL).
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getDailySalesRaw() {
        return em.createNativeQuery("""
            SELECT created_at, COUNT(id), SUM(total_amount)
            FROM sales_orders
            WHERE status <> 'CANCELLED'
            GROUP BY created_at
            ORDER BY created_at DESC
        """).getResultList();
    }

    /**
     * Récupère les données brutes pour les produits les plus vendus (Native SQL).
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getTopProductsRaw() {
        return em.createNativeQuery("""
            SELECT i.quantity, i.unit_price, i.product_id, p.name
            FROM sales_order_items i
            JOIN catalog_products p ON p.id = i.product_id
            JOIN sales_orders o ON o.id = i.order_id
            WHERE o.status <> 'CANCELLED'
        """).getResultList();
    }
}
