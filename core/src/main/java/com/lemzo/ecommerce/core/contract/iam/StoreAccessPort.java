package com.lemzo.ecommerce.core.contract.iam;

import java.util.UUID;

/**
 * Port pour vérifier l'accès aux boutiques (implémenté par IAM).
 */
@FunctionalInterface
public interface StoreAccessPort {
    /**
     * Vérifie si l'utilisateur possède ou gère la boutique.
     */
    boolean canAccessStore(UUID userId, UUID storeId);
}
