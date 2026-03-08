-- Améliorations IAM : Profil et Paiement
ALTER TABLE iam_users ADD COLUMN first_name varchar(100);
ALTER TABLE iam_users ADD COLUMN last_name varchar(100);
ALTER TABLE iam_users ADD COLUMN payment_methods jsonb DEFAULT '[]';

-- Table pour les adresses (plus flexible que ElementCollection si on veut du CRUD séparé)
CREATE TABLE iam_user_addresses (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id uuid NOT NULL REFERENCES iam_users(id),
    label varchar(50), -- ex: Domicile, Bureau
    street varchar(255) NOT NULL,
    city varchar(100) NOT NULL,
    postal_code varchar(20),
    country varchar(100) NOT NULL DEFAULT 'Sénégal',
    is_default boolean DEFAULT false,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone
);
