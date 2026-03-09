-- Activation des extensions pour PostgreSQL 18
CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =============================================================================
-- 1. MODULE IAM
-- =============================================================================

CREATE TABLE iam_users (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    username varchar(50) NOT NULL UNIQUE,
    email varchar(255) NOT NULL UNIQUE,
    password varchar(255) NOT NULL,
    first_name varchar(100),
    last_name varchar(100),
    enabled boolean NOT NULL DEFAULT true,
    -- JSONB optimisé (Postgres 18 améliore le traitement parallèle du JSONB)
    payment_methods jsonb DEFAULT '[]',
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone
);

CREATE TABLE iam_user_addresses (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id uuid NOT NULL REFERENCES iam_users(id) ON DELETE CASCADE,
    label varchar(50),
    street varchar(255) NOT NULL,
    city varchar(100) NOT NULL,
    zip_code varchar(20),
    country varchar(100) NOT NULL DEFAULT 'Sénégal',
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE iam_permissions (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    resource_type varchar(50) NOT NULL,
    action varchar(50) NOT NULL,
    UNIQUE (resource_type, action)
);

CREATE TABLE iam_roles (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    name varchar(100) NOT NULL UNIQUE,
    description text,
    is_system_role boolean NOT NULL DEFAULT false
);

CREATE TABLE iam_role_permissions (
    role_id uuid NOT NULL REFERENCES iam_roles(id) ON DELETE CASCADE,
    permission_id uuid NOT NULL REFERENCES iam_permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE iam_user_roles (
    user_id uuid NOT NULL REFERENCES iam_users(id) ON DELETE CASCADE,
    role_id uuid NOT NULL REFERENCES iam_roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- =============================================================================
-- 2. MODULE CATALOG (PostgreSQL 18 Optimized)
-- =============================================================================

CREATE TABLE catalog_categories (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    name varchar(255) NOT NULL UNIQUE,
    slug varchar(255) NOT NULL UNIQUE,
    description text,
    parent_id uuid REFERENCES catalog_categories(id)
);

CREATE TABLE catalog_products (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    store_id uuid NOT NULL,
    name varchar(255) NOT NULL,
    slug varchar(255) NOT NULL UNIQUE,
    sku varchar(100) NOT NULL UNIQUE,
    description text,
    price numeric(19,4) NOT NULL,
    currency varchar(3) NOT NULL DEFAULT 'XOF',
    weight numeric(10,3) DEFAULT 0,
    is_active boolean NOT NULL DEFAULT true,
    category_id uuid REFERENCES catalog_categories(id),
    attributes jsonb DEFAULT '{}',
    shipping_config jsonb DEFAULT '{}',
    view_count bigint DEFAULT 0,
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 3. MODULE SALES (Physical Columns for reliability)
-- =============================================================================

CREATE TABLE sales_orders (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id uuid NOT NULL REFERENCES iam_users(id),
    order_number varchar(100) NOT NULL UNIQUE,
    status varchar(50) NOT NULL,
    total_price numeric(19,4) NOT NULL DEFAULT 0,
    shipping_cost numeric(19,4) NOT NULL DEFAULT 0,
    discount_amount numeric(19,4) NOT NULL DEFAULT 0,
    coupon_code varchar(50),
    shipping_street varchar(255),
    shipping_city varchar(100),
    shipping_zip_code varchar(20),
    shipping_country varchar(100),
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sales_order_items (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    order_id uuid NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    product_id uuid NOT NULL REFERENCES catalog_products(id),
    store_id uuid NOT NULL,
    quantity int NOT NULL,
    unit_price numeric(19,4) NOT NULL,
    weight numeric(10,3),
    -- PostgreSQL 18 Virtual Column
    subtotal numeric(19,4) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 4. MODULE MARKETING (PostgreSQL 18 Temporal Constraints)
-- =============================================================================

CREATE TABLE marketing_coupons (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    code varchar(50) NOT NULL,
    type varchar(20) NOT NULL,
    value numeric(19,4) NOT NULL,
    start_date timestamp with time zone NOT NULL,
    end_date timestamp with time zone NOT NULL,
    usage_limit int,
    used_count int NOT NULL DEFAULT 0,
    is_active boolean NOT NULL DEFAULT true,
    -- PostgreSQL 18: Generated TSTZRANGE for Temporal Constraint
    validity_period tstzrange GENERATED ALWAYS AS (tstzrange(start_date, end_date, '[)')) STORED,
    -- Contrainte Temporelle PostgreSQL 18 : Garantit qu'un même code n'a pas de périodes qui se chevauchent
    CONSTRAINT marketing_coupons_temporal_unique UNIQUE (code, validity_period WITHOUT OVERLAPS)
);

CREATE TABLE marketing_product_promotions (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    product_id uuid NOT NULL REFERENCES catalog_products(id) ON DELETE CASCADE,
    discount_value numeric(19,4) NOT NULL,
    start_date timestamp with time zone NOT NULL,
    end_date timestamp with time zone NOT NULL,
    validity_period tstzrange GENERATED ALWAYS AS (tstzrange(start_date, end_date, '[)')) STORED,
    -- Exclusion temporelle native PostgreSQL 18
    CONSTRAINT promo_temporal_unique UNIQUE (product_id, validity_period WITHOUT OVERLAPS)
);

-- =============================================================================
-- 5. INVENTORY & SHIPPING
-- =============================================================================

CREATE TABLE inventory_stocks (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    product_id uuid NOT NULL UNIQUE REFERENCES catalog_products(id) ON DELETE CASCADE,
    quantity int NOT NULL DEFAULT 0,
    low_stock_threshold int NOT NULL DEFAULT 5,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shipping_shipments (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    order_id uuid NOT NULL UNIQUE REFERENCES sales_orders(id),
    tracking_number varchar(100) NOT NULL UNIQUE,
    status varchar(50) NOT NULL,
    carrier varchar(100) NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);
