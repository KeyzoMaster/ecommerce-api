-- Amélioration des commandes : Livraison
ALTER TABLE sales_orders ADD COLUMN shipping_method varchar(50);
ALTER TABLE sales_orders ADD COLUMN shipping_cost numeric(19,4) DEFAULT 0;

-- Mise à jour de la colonne virtuelle total_price pour inclure les frais de port
-- Note: PostgreSQL 18 permet de redéfinir les colonnes générées
ALTER TABLE sales_orders DROP COLUMN total_price;
ALTER TABLE sales_orders ADD COLUMN total_price numeric(19,4) 
    GENERATED ALWAYS AS (shipping_cost) VIRTUAL; 
