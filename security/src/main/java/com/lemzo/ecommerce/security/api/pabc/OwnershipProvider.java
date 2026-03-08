package com.lemzo.ecommerce.security.api.pabc;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import java.util.UUID;

/**
 * Interface SPI permettant de charger une ressource et de vérifier son propriétaire.
 */
public interface OwnershipProvider {
    /**
     * Retourne le type de ressource géré par ce provider.
     */
    ResourceType getResourceType();

    /**
     * Retourne l'ID du propriétaire de la ressource spécifiée.
     */
    UUID getOwnerId(UUID resourceId);
}
