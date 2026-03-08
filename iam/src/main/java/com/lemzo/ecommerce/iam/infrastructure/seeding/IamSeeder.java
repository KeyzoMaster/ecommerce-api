package com.lemzo.ecommerce.iam.infrastructure.seeding;

import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import com.lemzo.ecommerce.iam.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * Seeder pour les utilisateurs initiaux.
 */
@ApplicationScoped
public class IamSeeder implements DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(IamSeeder.class.getName());

    @Inject
    private UserService userService;

    @Override
    public void seed() {
        if (userService.findByIdentifier("admin").isEmpty()) {
            LOGGER.info("Création de l'utilisateur admin par défaut...");
            userService.createUser("admin", "admin@lemzo.com", "admin1234");
        }
    }

    @Override
    public int priority() {
        return 1; // Les utilisateurs sont créés en premier
    }
}
