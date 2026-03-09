-- =============================================================================
-- 6. RECHERCHE & ANALYTICS (PostgreSQL 18 Optimized)
-- =============================================================================

-- Recherche Plein Texte (FTS) optimisée
ALTER TABLE catalog_products ADD COLUMN search_vector tsvector 
    GENERATED ALWAYS AS (
        setweight(to_tsvector('french', coalesce(name, '')), 'A') || 
        setweight(to_tsvector('french', coalesce(description, '')), 'B')
    ) STORED;

CREATE INDEX idx_products_search ON catalog_products USING GIN(search_vector);

-- Index Trigramme pour recherche approximative ultra-rapide
CREATE INDEX idx_products_name_trgm ON catalog_products USING GIN (name gin_trgm_ops);

-- PostgreSQL 18 JSONB Optimization: Index sur les attributs dynamiques
-- Permet des recherches parallèles ultra-rapides sur les attributs
CREATE INDEX idx_products_attributes_jsonb ON catalog_products USING GIN (attributes);
CREATE INDEX idx_products_shipping_config_jsonb ON catalog_products USING GIN (shipping_config);

-- Vue Analytics consolidée
-- Profite des améliorations de performance de PostgreSQL 18 sur les agrégations complexes
CREATE OR REPLACE VIEW analytics_product_performance AS
SELECT 
    p.id as product_id,
    p.name as product_name,
    p.view_count,
    COALESCE(SUM(oi.quantity), 0) as sale_count,
    COALESCE(SUM(oi.subtotal), 0) as total_revenue,
    CASE 
        WHEN p.view_count > 0 THEN (COALESCE(SUM(oi.quantity), 0)::numeric / p.view_count::numeric) * 100 
        ELSE 0 
    END as conversion_rate
FROM catalog_products p
LEFT JOIN sales_order_items oi ON oi.product_id = p.id
GROUP BY p.id, p.name, p.view_count;

-- Index de couverture pour les jointures de commande (Index-Only Scans)
CREATE INDEX idx_order_items_order_id_covering ON sales_order_items(order_id) INCLUDE (product_id, quantity, subtotal);
