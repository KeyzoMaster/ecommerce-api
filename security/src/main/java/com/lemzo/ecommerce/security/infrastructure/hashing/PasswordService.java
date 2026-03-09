package com.lemzo.ecommerce.security.infrastructure.hashing;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service de hachage des mots de passe utilisant Argon2.
 */
@ApplicationScoped
public class PasswordService {

    private static final int ITERATIONS = 2;
    private static final int MEMORY = 65_536;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    /**
     * Hache un mot de passe en clair.
     */
    public String hash(final char[] password) {
        try {
            return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password);
        } finally {
            argon2.wipeArray(password);
        }
    }

    /**
     * Vérifie un mot de passe par rapport à un hachage.
     */
    public boolean verify(final String hash, final char[] password) {
        try {
            return argon2.verify(hash, password);
        } finally {
            argon2.wipeArray(password);
        }
    }
}
