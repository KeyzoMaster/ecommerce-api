package com.lemzo.ecommerce.core.api.security;

import java.util.EnumSet;
import java.util.Set;

/**
 * Définit les actions possibles avec logique d'implicité.
 */
public enum PbacAction {
    READ, CREATE, UPDATE, DELETE, MANAGE, VIEW_ANALYTICS, APPLY_COUPON;

    /**
     * Retourne l'ensemble des actions qui accordent implicitement cette action.
     */
    public Set<PbacAction> getGrantingActions() {
        final Set<PbacAction> granting = EnumSet.of(this, MANAGE);
        
        switch (this) {
            case READ, VIEW_ANALYTICS -> granting.add(MANAGE);
            default -> {}
        }
        
        return granting;
    }
}
