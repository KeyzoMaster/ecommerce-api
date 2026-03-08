-- Amélioration des items de commande : Poids et Config
ALTER TABLE sales_order_items ADD COLUMN weight numeric(10,3);
ALTER TABLE sales_order_items ADD COLUMN shipping_config jsonb;
