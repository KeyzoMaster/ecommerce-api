-- =============================================================================
-- 6. RECHERCHE & ANALYTICS (PostgreSQL 18 Optimized)
-- =============================================================================

-- Recherche Plein Texte (FTS) avec support natif PG 18
ALTER TABLE catalog_products ADD COLUMN IF NOT EXISTS search_vector tsvector 
GENERATED ALWAYS AS (
    setweight(to_tsvector('french', coalesce(name, '')), 'A') ||
    setweight(to_tsvector('french', coalesce(description, '')), 'B')
) STORED;

CREATE INDEX idx_products_search ON catalog_products USING GIN (search_vector);

-- PostgreSQL 18 JSONB Optimization: Index sur les attributs dynamiques
-- Utilisation de jsonb_path_ops pour des performances optimales sur JSONB
CREATE INDEX idx_products_attributes_jsonb ON catalog_products USING GIN (attributes jsonb_path_ops);
CREATE INDEX idx_products_shipping_config_jsonb ON catalog_products USING GIN (shipping_config jsonb_path_ops);

-- Vue Analytics consolidée
CREATE OR REPLACE VIEW analytics_product_performance AS
SELECT 
    p.id as product_id,
    p.name as product_name,
    count(oi.id) as total_sales_count,
    sum(oi.quantity) as total_quantity_sold,
    sum(oi.subtotal) as total_revenue,
    p.view_count,
    CASE WHEN p.view_count > 0 THEN (count(oi.id)::float / p.view_count) * 100 ELSE 0 END as conversion_rate
FROM catalog_products p
LEFT JOIN sales_order_items oi ON oi.product_id = p.id
GROUP BY p.id, p.name, p.view_count;

-- Index de couverture pour les jointures de commande
CREATE INDEX idx_order_items_order_id_covering ON sales_order_items(order_id) INCLUDE (product_id, quantity, subtotal);
