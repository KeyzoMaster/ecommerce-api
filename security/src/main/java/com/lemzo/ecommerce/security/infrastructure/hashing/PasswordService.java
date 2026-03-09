package com.lemzo.ecommerce.security.infrastructure.hashing;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service de hachage des mots de passe utilisant BCrypt.
 */
@ApplicationScoped
public class PasswordService {

    private static final Logger LOGGER = Logger.getLogger(PasswordService.class.getName());
    private static final int LOG_ROUNDS = 12;

    public PasswordService() {
        // Required by CDI
    }

    /**
     * Hache un mot de passe en clair.
     */
    public String hash(final char[] password) {
        return BCrypt.hashpw(new String(password), BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Vérifie un mot de passe par rapport à un hachage.
     */
    public boolean verify(final String hash, final char[] password) {
        return Optional.ofNullable(hash)
                .flatMap(h -> Optional.ofNullable(password)
                        .map(p -> {
                            try {
                                return BCrypt.checkpw(new String(p), h);
                            } catch (Exception e) {
                                LOGGER.warning("Erreur verification: " + e.getMessage());
                                return false;
                            }
                        }))
                .orElse(false);
    }
}
