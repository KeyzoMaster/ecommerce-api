-- Ajout du support de recherche plein texte sur les produits
ALTER TABLE catalog_products ADD COLUMN search_vector tsvector 
    GENERATED ALWAYS AS (
        setweight(to_tsvector('french', coalesce(name, '')), 'A') || 
        setweight(to_tsvector('french', coalesce(description, '')), 'B')
    ) STORED;

CREATE INDEX idx_products_search ON catalog_products USING GIN(search_vector);
