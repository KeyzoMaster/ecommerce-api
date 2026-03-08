package com.lemzo.ecommerce.core.api.security;

import java.util.Map;
import java.util.Set;
import static com.lemzo.ecommerce.core.api.security.ResourceType.*;
import static com.lemzo.ecommerce.core.api.security.PbacAction.*;

/**
 * Définition des rôles standards du système.
 */
public enum StandardRole {
    SUPER_ADMIN(Map.of(
        PLATFORM, Set.of(MANAGE)
    )),
    CLIENT(Map.of(
        CATALOG, Set.of(READ),
        SALES, Set.of(READ, CREATE)
    )),
    STORE_OWNER(Map.of(
        CATALOG, Set.of(READ, CREATE, UPDATE, DELETE),
        SALES, Set.of(READ, UPDATE, APPROVE),
        INVENTORY, Set.of(READ, UPDATE, MANAGE_STOCK),
        ANALYTICS, Set.of(VIEW_ANALYTICS)
    ));

    private final Map<ResourceType, Set<PbacAction>> permissions;

    StandardRole(Map<ResourceType, Set<PbacAction>> permissions) {
        this.permissions = permissions;
    }

    public Map<ResourceType, Set<PbacAction>> getPermissions() {
        return permissions;
    }
}
