package com.lemzo.ecommerce.api.infrastructure.migration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import org.flywaydb.core.Flyway;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service déclenchant les migrations Flyway au démarrage de l'application.
 */
@ApplicationScoped
public class FlywayMigrationService {

    private static final Logger LOGGER = Logger.getLogger(FlywayMigrationService.class.getName());

    public FlywayMigrationService() {
        // Required by CDI
    }

    /**
     * Observe l'initialisation du contexte applicatif pour lancer Flyway.
     */
    public void onStart(@Observes @Initialized(ApplicationScoped.class) final Object init) {
        final String url = Optional.ofNullable(System.getenv("DB_URL"))
                .orElse("jdbc:postgresql://postgres:5432/ecommerce_db");
        
        final String user = Optional.ofNullable(System.getenv("DB_USER"))
                .orElse("e_user");
        
        final String password = Optional.ofNullable(System.getenv("DB_PASSWORD"))
                .orElse("e_password");

        LOGGER.info(() -> "Démarrage des migrations Flyway sur : " + url + " (User: " + user + ")");
        
        try {
            final Flyway flyway = Flyway.configure()
                    .dataSource(url, user, password)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .load();

            LOGGER.info("Exécution de Flyway Repair...");
            flyway.repair();
            
            LOGGER.info("Exécution de Flyway Migrate...");
            flyway.migrate();
            
            LOGGER.info("Migrations Flyway terminées avec succès.");
        } catch (final Throwable throwable) {
            LOGGER.log(Level.SEVERE, "ERREUR CRITIQUE FLYWAY : " + throwable.getMessage(), throwable);
            throw new RuntimeException("Migration failed: " + throwable.getMessage(), throwable);
        }
    }
}
