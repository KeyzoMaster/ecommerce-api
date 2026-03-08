-- Rôles et Permissions
CREATE TABLE iam_permissions (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    resource_type varchar(50) NOT NULL,
    action varchar(50) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone,
    UNIQUE (resource_type, action)
);

CREATE TABLE iam_roles (
    id uuid PRIMARY KEY DEFAULT uuidv7(),
    name varchar(100) NOT NULL UNIQUE,
    description text,
    is_system_role boolean NOT NULL DEFAULT false,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    deleted_at timestamp with time zone
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

CREATE TABLE iam_user_adhoc_permissions (
    user_id uuid NOT NULL REFERENCES iam_users(id) ON DELETE CASCADE,
    permission_id uuid NOT NULL REFERENCES iam_permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, permission_id)
);

-- Initialisation des permissions de base (Matching ResourceType & PbacAction enums)
INSERT INTO iam_permissions (id, resource_type, action, created_at) VALUES 
(uuidv7(), 'CATALOG', 'READ', now()),
(uuidv7(), 'CATALOG', 'CREATE', now()),
(uuidv7(), 'CATALOG', 'UPDATE', now()),
(uuidv7(), 'CATALOG', 'DELETE', now()),
(uuidv7(), 'SALES', 'READ', now()),
(uuidv7(), 'SALES', 'CREATE', now()),
(uuidv7(), 'PLATFORM', 'MANAGE', now());

-- Création du rôle Admin système par défaut
INSERT INTO iam_roles (id, name, description, is_system_role, created_at) VALUES 
(uuidv7(), 'SUPER_ADMIN', 'Administrateur complet de la plateforme', true, now());

-- Attribution de la permission platform:manage au rôle SUPER_ADMIN
INSERT INTO iam_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM iam_roles r, iam_permissions p 
WHERE r.name = 'SUPER_ADMIN' AND p.resource_type = 'PLATFORM' AND p.action = 'MANAGE';
