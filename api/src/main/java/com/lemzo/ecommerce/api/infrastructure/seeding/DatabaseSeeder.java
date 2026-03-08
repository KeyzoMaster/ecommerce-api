package com.lemzo.ecommerce.api.infrastructure.seeding;

import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * Orchestrateur déclenchant le seeding si le profil est approprié.
 */
@ApplicationScoped
public class DatabaseSeeder {

    private static final Logger LOGGER = Logger.getLogger(DatabaseSeeder.class.getName());

    @Inject
    private Instance<DataSeeder> seeders;

    @ConfigProperty(name = "app.seeding.enabled", defaultValue = "false")
    private boolean seedingEnabled;

    @ConfigProperty(name = "mp.config.profile", defaultValue = "prod")
    private String activeProfile;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        if (!seedingEnabled) {
            LOGGER.info("Seeding désactivé (app.seeding.enabled=false).");
            return;
        }

        LOGGER.info("Démarrage du seeding pour le profil : " + activeProfile);

        seeders.stream()
                .sorted(Comparator.comparingInt(DataSeeder::priority))
                .forEach(seeder -> {
                    LOGGER.info("Exécution du seeder : " + seeder.getClass().getSimpleName());
                    seeder.seed();
                });

        LOGGER.info("Seeding terminé.");
    }
}
