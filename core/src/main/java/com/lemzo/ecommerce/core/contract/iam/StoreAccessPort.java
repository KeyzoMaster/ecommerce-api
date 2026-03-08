package com.lemzo.ecommerce.core.contract.iam;

import java.util.Optional;
import java.util.UUID;

/**
 * Port pour accéder aux informations de base des boutiques depuis les autres modules.
 */
public interface StoreAccessPort {
    /**
     * Retourne l'identifiant du propriétaire d'une boutique.
     */
    Optional<UUID> getStoreOwnerId(UUID storeId);
}
