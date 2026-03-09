-- =============================================================================
-- 7. DONNÉES INITIALES (Permissions & Rôles)
-- =============================================================================

-- Initialisation des permissions de base
INSERT INTO iam_permissions (id, resource_type, action) VALUES 
(uuidv7(), 'CATALOG', 'READ'),
(uuidv7(), 'CATALOG', 'CREATE'),
(uuidv7(), 'CATALOG', 'UPDATE'),
(uuidv7(), 'CATALOG', 'DELETE'),
(uuidv7(), 'PRODUCT', 'UPDATE'),
(uuidv7(), 'SALES', 'READ'),
(uuidv7(), 'SALES', 'MANAGE'),
(uuidv7(), 'ORDER', 'READ'),
(uuidv7(), 'ORDER', 'CREATE'),
(uuidv7(), 'INVENTORY', 'READ'),
(uuidv7(), 'INVENTORY', 'UPDATE'),
(uuidv7(), 'ANALYTICS', 'VIEW_ANALYTICS'),
(uuidv7(), 'MARKETING', 'CREATE'),
(uuidv7(), 'MARKETING', 'READ'),
(uuidv7(), 'MARKETING', 'APPLY_COUPON'),
(uuidv7(), 'PLATFORM', 'MANAGE');

-- Création des rôles système
INSERT INTO iam_roles (id, name, description, is_system_role) VALUES 
(uuidv7(), 'SUPER_ADMIN', 'Administrateur complet de la plateforme', true),
(uuidv7(), 'STORE_OWNER', 'Propriétaire de boutique', true),
(uuidv7(), 'CLIENT', 'Client standard', true);

-- Attribution de platform:manage au SUPER_ADMIN
INSERT INTO iam_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM iam_roles r, iam_permissions p 
WHERE r.name = 'SUPER_ADMIN' AND p.resource_type = 'PLATFORM' AND p.action = 'MANAGE';

-- Exemple de Coupon Temporel PostgreSQL 18
-- Un coupon valide uniquement pour le mois de Mars 2026
INSERT INTO marketing_coupons (id, code, type, value, start_date, end_date, created_at)
VALUES (uuidv7(), 'MARS2026', 'PERCENTAGE', 15.00, '2026-03-01 00:00:00+00', '2026-04-01 00:00:00+00', now());
