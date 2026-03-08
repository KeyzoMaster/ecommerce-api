package com.lemzo.ecommerce.security.infrastructure.hashing;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service de hachage des mots de passe utilisant Argon2.
 */
@ApplicationScoped
public class PasswordService {

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    // Paramètres recommandés (à ajuster selon les performances du serveur)
    private static final int ITERATIONS = 2;
    private static final int MEMORY = 65536;
    private static final int PARALLELISM = 1;

    /**
     * Hache un mot de passe en clair.
     */
    public String hash(char[] password) {
        try {
            return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password);
        } finally {
            argon2.wipeArray(password);
        }
    }

    /**
     * Vérifie un mot de passe par rapport à un hash.
     */
    public boolean verify(String hash, char[] password) {
        try {
            return argon2.verify(hash, password);
        } finally {
            argon2.wipeArray(password);
        }
    }
}
