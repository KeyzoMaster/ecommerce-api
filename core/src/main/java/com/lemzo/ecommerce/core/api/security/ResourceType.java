package com.lemzo.ecommerce.core.api.security;

import static com.lemzo.ecommerce.core.api.security.PbacAction.*;
import java.util.Set;

/**
 * Hiérarchie des types de ressources du système.
 */
public enum ResourceType {

    /** Racine système. */
    PLATFORM(null, Set.of(MANAGE, READ, EXPORT)),

    /** Gestion des utilisateurs (Enfant de PLATFORM). */
    IAM(PLATFORM, Set.of(READ, CREATE, UPDATE, DELETE, MANAGE)),

    /** Gestion des boutiques (Enfant de PLATFORM). */
    STORE(PLATFORM, Set.of(READ, CREATE, UPDATE, DELETE, MANAGE)),

    /** Catalogue racine. */
    CATALOG(PLATFORM, Set.of(READ, CREATE, UPDATE, DELETE, EXPORT, IMPORT)),

    /** Produits (Enfant de CATALOG). */
    PRODUCT(CATALOG, Set.of(READ, CREATE, UPDATE, DELETE)),

    /** Ventes racine. */
    SALES(PLATFORM, Set.of(READ, CREATE, UPDATE, DELETE, APPROVE, REJECT, CANCEL, EXPORT)),

    /** Commandes (Enfant de SALES). */
    ORDER(SALES, Set.of(READ, CREATE, UPDATE)),

    /** Stocks (Enfant de PRODUCT). */
    INVENTORY(PRODUCT, Set.of(READ, UPDATE, MANAGE_STOCK, EXPORT)),

    /** Marketing racine. */
    MARKETING(PLATFORM, Set.of(READ, CREATE, UPDATE, DELETE, APPLY_COUPON)),

    /** Analytics (Enfant de PLATFORM). */
    ANALYTICS(PLATFORM, Set.of(READ, VIEW_ANALYTICS, EXPORT));

    private final ResourceType parent;
    private final Set<PbacAction> supportedActions;

    ResourceType(ResourceType parent, Set<PbacAction> supportedActions) {
        this.parent = parent;
        this.supportedActions = supportedActions;
    }

    public ResourceType getParent() {
        return parent;
    }

    public boolean supports(PbacAction action) {
        return supportedActions.contains(action);
    }
}
