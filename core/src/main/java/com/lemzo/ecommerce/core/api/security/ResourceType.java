package com.lemzo.ecommerce.core.api.security;

import static com.lemzo.ecommerce.core.api.security.PbacAction.*;
import java.util.Set;

/**
 * Hiérarchie des types de ressources du système.
 */
public enum ResourceType {

    PLATFORM(null, Set.of(MANAGE)),
    STORE(PLATFORM, Set.of(READ, CREATE, UPDATE, DELETE)),
    CATALOG(STORE, Set.of(READ, CREATE, UPDATE, DELETE)),
    PRODUCT(CATALOG, Set.of(READ, UPDATE, DELETE)),
    SALES(STORE, Set.of(READ, MANAGE)),
    ORDER(SALES, Set.of(READ, CREATE, UPDATE)),
    INVENTORY(STORE, Set.of(READ, UPDATE)),
    MARKETING(STORE, Set.of(READ, CREATE, APPLY_COUPON)),
    ANALYTICS(STORE, Set.of(VIEW_ANALYTICS));

    private final ResourceType parent;
    private final Set<PbacAction> supportedActions;

    ResourceType(final ResourceType parent, final Set<PbacAction> supportedActions) {
        this.parent = parent;
        this.supportedActions = supportedActions;
    }

    public ResourceType getParent() {
        return parent;
    }

    public boolean supports(final PbacAction action) {
        return supportedActions.contains(action);
    }
}
