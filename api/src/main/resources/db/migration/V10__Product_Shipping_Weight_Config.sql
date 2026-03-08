-- Amélioration du catalog : Poids et Configuration Livraison
ALTER TABLE catalog_products ADD COLUMN weight numeric(10,3) DEFAULT 0; -- en kg
ALTER TABLE catalog_products ADD COLUMN shipping_config jsonb DEFAULT '{}'; 
-- shipping_config: {"allowed_methods": ["STANDARD", "EXPRESS"], "base_rates": {"STANDARD": 1500, "EXPRESS": 4000}}
