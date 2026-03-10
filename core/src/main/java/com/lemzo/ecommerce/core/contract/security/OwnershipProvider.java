package com.lemzo.ecommerce.core.contract.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import java.util.UUID;

/**
 * Interface pour fournir le propriétaire d'une ressource.
 * Utilisé par le moteur Hybrid PBAC.
 */
public interface OwnershipProvider {
    /**
     * Retourne le type de ressource géré par ce fournisseur.
     */
    ResourceType getResourceType();

    /**
     * Retourne l'identifiant de l'utilisateur propriétaire de la ressource.
     */
    UUID getOwnerId(UUID resourceId);

    /**
     * Retourne l'identifiant de la ressource parente (pour la récursivité).
     */
    default UUID getParentId(UUID resourceId) {
        return null;
    }
}
