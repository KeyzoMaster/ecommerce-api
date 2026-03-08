package com.lemzo.ecommerce.core.api.security;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

/**
 * Interface pour transporter les informations de l'utilisateur authentifié de manière agnostique.
 */
public interface AuthenticatedUser extends Principal {
    UUID getUserId();
    String getEmail();
    Set<String> getPermissions();
}
