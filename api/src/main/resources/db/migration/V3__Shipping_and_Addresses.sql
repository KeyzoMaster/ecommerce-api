-- Shipping
CREATE TABLE shipping_shipments (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    order_id uuid NOT NULL UNIQUE REFERENCES sales_orders(id),
    tracking_number varchar(100) NOT NULL UNIQUE,
    status varchar(50) NOT NULL,
    carrier varchar(100) NOT NULL,
    estimated_delivery_date timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone
);

-- User Addresses (Element Collection)
CREATE TABLE iam_user_addresses (
    user_id uuid NOT NULL REFERENCES iam_users(id) ON DELETE CASCADE,
    street varchar(255) NOT NULL,
    city varchar(100) NOT NULL,
    zip_code varchar(20) NOT NULL,
    country varchar(100) NOT NULL
);
