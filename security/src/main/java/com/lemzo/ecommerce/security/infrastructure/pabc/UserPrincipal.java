package com.lemzo.ecommerce.security.infrastructure.pabc;

import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import java.util.Set;
import java.util.UUID;

/**
 * Principal immuable transportant l'identité et les droits.
 * Utilise Java 25 Records pour une sécurité maximale (non-modifiable).
 */
public record UserPrincipal(
    UUID userId,
    String email,
    Set<String> permissions
) implements AuthenticatedUser {

    public UserPrincipal {
        // Protection supplémentaire : copie défensive pour l'immuabilité réelle
        permissions = Set.copyOf(permissions);
    }

    @Override
    public UUID getUserId() {
        return userId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public String getName() {
        return email;
    }
}
