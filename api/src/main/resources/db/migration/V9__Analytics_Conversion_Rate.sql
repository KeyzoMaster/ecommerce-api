-- Ajout du support pour le taux de conversion (vues produits)
ALTER TABLE catalog_products ADD COLUMN view_count bigint DEFAULT 0;

-- Vue pour simplifier le calcul des analytics
CREATE OR REPLACE VIEW analytics_conversion_rates AS
SELECT 
    p.id as product_id,
    p.name as product_name,
    p.view_count,
    COALESCE(SUM(oi.quantity), 0) as sale_count,
    CASE 
        WHEN p.view_count > 0 THEN (COALESCE(SUM(oi.quantity), 0)::numeric / p.view_count::numeric) * 100 
        ELSE 0 
    END as conversion_rate
FROM catalog_products p
LEFT JOIN sales_order_items oi ON oi.product_id = p.id
GROUP BY p.id, p.name, p.view_count;
