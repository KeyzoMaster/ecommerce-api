package com.lemzo.ecommerce.iam.domain;

import java.util.Collections;
import java.util.HashSet;

/**
 * Factory pour la création sécurisée d'utilisateurs.
 * Garantit l'état initial valide de l'entité.
 */
public final class UserFactory {

    private UserFactory() {
        // Classe utilitaire
    }

    /**
     * Crée un nouvel utilisateur actif avec les champs de base.
     */
    public static User create(final String username, final String email, final String hashedPassword) {
        final User user = new User(username, email, hashedPassword);
        user.setEnabled(true);
        user.setRoles(new HashSet<>());
        user.setAdhocPermissions(new HashSet<>());
        user.setAddresses(Collections.emptyList());
        return user;
    }
}
