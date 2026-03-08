-- Amélioration de la recherche avancée
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Index trigram pour la recherche "floue" (typos) sur le nom
CREATE INDEX idx_products_name_trgm ON catalog_products USING GIST (name gist_trgm_ops);

-- Mise à jour du vecteur de recherche pour inclure le SKU (Poids C)
-- Note: On recrée la colonne générée pour inclure le SKU
ALTER TABLE catalog_products DROP COLUMN search_vector;
ALTER TABLE catalog_products ADD COLUMN search_vector tsvector 
    GENERATED ALWAYS AS (
        setweight(to_tsvector('french', coalesce(name, '')), 'A') || 
        setweight(to_tsvector('french', coalesce(description, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(sku, '')), 'C')
    ) STORED;

-- On recrée l'index GIN
CREATE INDEX idx_products_search ON catalog_products USING GIN(search_vector);
