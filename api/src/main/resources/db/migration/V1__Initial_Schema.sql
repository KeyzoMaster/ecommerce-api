-- Activation des extensions nécessaires
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- IAM
CREATE TABLE iam_users (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    username varchar(50) NOT NULL UNIQUE,
    email varchar(255) NOT NULL UNIQUE,
    password varchar(255) NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    email_verified_at timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone
);

-- Catalog
CREATE TABLE catalog_categories (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    name varchar(255) NOT NULL UNIQUE,
    slug varchar(255) NOT NULL UNIQUE,
    description text,
    parent_id uuid REFERENCES catalog_categories(id),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone
);

CREATE TABLE catalog_products (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    name varchar(255) NOT NULL,
    slug varchar(255) NOT NULL UNIQUE,
    sku varchar(100) NOT NULL UNIQUE,
    description text,
    price numeric(19,4) NOT NULL,
    currency varchar(3) NOT NULL DEFAULT 'XOF',
    is_active boolean NOT NULL DEFAULT true,
    category_id uuid REFERENCES catalog_categories(id),
    attributes jsonb,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone
);

-- Sales
CREATE TABLE sales_orders (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id uuid NOT NULL REFERENCES iam_users(id),
    order_number varchar(100) NOT NULL UNIQUE,
    status varchar(50) NOT NULL,
    currency varchar(3) NOT NULL DEFAULT 'XOF',
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone
);

-- Utilisation des colonnes virtuelles de PostgreSQL 18
ALTER TABLE sales_orders ADD COLUMN total_price numeric(19,4) 
    GENERATED ALWAYS AS (0) VIRTUAL; -- Sera mis à jour par une vue ou calculé à la volée

CREATE TABLE sales_order_items (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    order_id uuid NOT NULL REFERENCES sales_orders(id),
    product_id uuid NOT NULL REFERENCES catalog_products(id),
    quantity int NOT NULL,
    unit_price numeric(19,4) NOT NULL,
    subtotal numeric(19,4) GENERATED ALWAYS AS (quantity * unit_price) VIRTUAL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone
);

-- Inventory
CREATE TABLE inventory_stocks (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    product_id uuid NOT NULL UNIQUE REFERENCES catalog_products(id),
    quantity int NOT NULL DEFAULT 0,
    low_stock_threshold int NOT NULL DEFAULT 5,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone
);

-- Marketing
CREATE TABLE marketing_coupons (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    code varchar(50) NOT NULL UNIQUE,
    type varchar(20) NOT NULL,
    value numeric(19,4) NOT NULL,
    start_date timestamp with time zone,
    end_date timestamp with time zone,
    usage_limit int,
    used_count int NOT NULL DEFAULT 0,
    is_active boolean NOT NULL DEFAULT true,
    is_expired boolean GENERATED ALWAYS AS (end_date < CURRENT_TIMESTAMP) VIRTUAL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone
);

CREATE TABLE marketing_product_promotions (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    product_id uuid NOT NULL REFERENCES catalog_products(id),
    discount_value numeric(19,4) NOT NULL,
    validity_period tstzrange NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone,
    -- Contrainte d'exclusion temporelle PostgreSQL 18
    EXCLUDE USING gist (product_id WITH =, validity_period WITHOUT OVERLAPS)
);
