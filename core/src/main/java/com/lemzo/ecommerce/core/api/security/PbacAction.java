package com.lemzo.ecommerce.core.api.security;

import java.util.EnumSet;
import java.util.Set;

/**
 * Définit les actions possibles avec logique d'implicité.
 */
public enum PbacAction {
    READ, CREATE, UPDATE, DELETE, MANAGE, 
    APPROVE, REJECT, CANCEL, EXPORT, IMPORT,
    VIEW_ANALYTICS, MANAGE_STOCK, APPLY_COUPON;

    /**
     * Retourne l'ensemble des actions qui accordent l'action cible.
     * Exemple: MANAGE accorde tout. UPDATE accorde READ.
     */
    public Set<PbacAction> getGrantingActions() {
        Set<PbacAction> granting = EnumSet.of(this);
        
        // MANAGE est le super-pouvoir
        if (this != MANAGE) {
            granting.add(MANAGE);
        }

        switch (this) {
            case READ -> {
                granting.add(UPDATE);
                granting.add(APPROVE);
                granting.add(EXPORT);
            }
            case VIEW_ANALYTICS -> {
                granting.add(MANAGE);
            }
            // Ajoutez d'autres règles d'implicité ici
            default -> {}
        }
        
        return granting;
    }
}
