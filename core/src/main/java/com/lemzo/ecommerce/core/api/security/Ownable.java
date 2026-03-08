package com.lemzo.ecommerce.core.api.security;

import java.util.UUID;

/**
 * Interface pour les entités ayant un propriétaire identifié.
 */
public interface Ownable {
    /**
     * Retourne l'identifiant de l'utilisateur propriétaire.
     */
    UUID getOwnerId();
}
