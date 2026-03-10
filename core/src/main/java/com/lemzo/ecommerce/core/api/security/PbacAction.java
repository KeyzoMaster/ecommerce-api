package com.lemzo.ecommerce.core.api.security;

import java.util.EnumSet;
import java.util.Set;

/**
 * Définit les actions possibles avec logique d'implicité.
 */
public enum PbacAction {
    READ, CREATE, UPDATE, DELETE, MANAGE, EXECUTE;

    /**
     * Retourne l'ensemble des actions qui, si possédées par l'utilisateur, 
     * accordent l'action demandée (this).
     */
    public Set<PbacAction> getRequiredPossessedActions() {
        // Posséder l'action elle-même ou MANAGE accorde toujours l'accès.
        final Set<PbacAction> required = EnumSet.of(this, MANAGE);
        
        // Logique d'implicité : Posséder UPDATE ou CREATE accorde souvent READ.
        if (this == READ) {
            required.add(UPDATE);
            required.add(CREATE);
            required.add(DELETE);
            required.add(EXECUTE);
        }
        
        return required;
    }
}
