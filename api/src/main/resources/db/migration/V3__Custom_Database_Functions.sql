-- =============================================================================
-- 8. WRAPPERS POUR COMPATIBILITÉ JPQL / JAKARTA DATA
-- =============================================================================

-- Wrapper pour la recherche plein texte (FTS)
CREATE OR REPLACE FUNCTION fts_match(vector tsvector, query_text text) 
RETURNS boolean AS $$
BEGIN
    RETURN vector @@ websearch_to_tsquery('french', query_text);
END;
$$ LANGUAGE plpgsql STABLE;

-- Wrapper pour le calcul du rang (Score de pertinence)
CREATE OR REPLACE FUNCTION fts_rank(vector tsvector, query_text text) 
RETURNS float4 AS $$
BEGIN
    RETURN ts_rank(vector, websearch_to_tsquery('french', query_text));
END;
$$ LANGUAGE plpgsql STABLE;

-- Wrapper pour l'extraction JSONB (Adapté pour colonnes TEXT)
CREATE OR REPLACE FUNCTION jsonb_attr(data_text text, key_name text) 
RETURNS text AS $$
BEGIN
    RETURN (data_text::jsonb) ->> key_name;
END;
$$ LANGUAGE plpgsql STABLE;

-- Wrapper pour la vérification de validité temporelle (Compatibilité @>)
CREATE OR REPLACE FUNCTION is_in_period(validity tstzrange) 
RETURNS boolean AS $$
BEGIN
    RETURN validity @> CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql STABLE;

-- Helper pour promouvoir un utilisateur (Assignation de rôle)
CREATE OR REPLACE FUNCTION promote_user(username_val text, role_name_val text) 
RETURNS void AS $$
BEGIN
    INSERT INTO iam_user_roles (user_id, role_id)
    SELECT u.id, r.id FROM iam_users u, iam_roles r
    WHERE u.username = username_val AND r.name = role_name_val
    ON CONFLICT DO NOTHING;
END;
$$ LANGUAGE plpgsql;
