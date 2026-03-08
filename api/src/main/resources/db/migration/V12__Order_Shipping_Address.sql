-- Ajout de l'adresse de livraison aux commandes
ALTER TABLE sales_orders ADD COLUMN shipping_street varchar(255);
ALTER TABLE sales_orders ADD COLUMN shipping_city varchar(100);
ALTER TABLE sales_orders ADD COLUMN shipping_zip_code varchar(20);
ALTER TABLE sales_orders ADD COLUMN shipping_country varchar(100);
