package com.lemzo.ecommerce.core.api.security;

import java.util.Map;
import java.util.Set;
import static com.lemzo.ecommerce.core.api.security.ResourceType.*;
import static com.lemzo.ecommerce.core.api.security.PbacAction.*;

/**
 * Définition des rôles standards du système.
 */
public enum StandardRole {
    ADMIN(Map.of(
        PLATFORM, Set.of(MANAGE)
    )),
    CLIENT(Map.of(
        CATALOG, Set.of(READ),
        SALES, Set.of(READ, CREATE)
    )),
    STORE_OWNER(Map.of(
        STORE, Set.of(READ, UPDATE),
        CATALOG, Set.of(READ, CREATE, UPDATE, DELETE),
        SALES, Set.of(READ, UPDATE, MANAGE),
        INVENTORY, Set.of(READ, UPDATE),
        ANALYTICS, Set.of(READ)
    ));

    private final Map<ResourceType, Set<PbacAction>> permissions;

    StandardRole(final Map<ResourceType, Set<PbacAction>> permissions) {
        this.permissions = Map.copyOf(permissions);
    }

    public Map<ResourceType, Set<PbacAction>> getPermissions() {
        return permissions;
    }
}
