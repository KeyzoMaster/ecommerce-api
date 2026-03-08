package com.lemzo.ecommerce.api.infrastructure.migration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.flywaydb.core.Flyway;
import java.util.logging.Logger;

/**
 * Service déclenchant les migrations Flyway au démarrage de l'application.
 */
@ApplicationScoped
public class FlywayMigrationService {

    private static final Logger LOGGER = Logger.getLogger(FlywayMigrationService.class.getName());

    @ConfigProperty(name = "DB_URL", defaultValue = "jdbc:postgresql://localhost:5432/ecommerce_db")
    private String url;

    @ConfigProperty(name = "DB_USER", defaultValue = "e_user")
    private String user;

    @ConfigProperty(name = "DB_PASSWORD", defaultValue = "e_password")
    private String password;

    /**
     * Observe l'initialisation du contexte applicatif pour lancer Flyway.
     */
    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        LOGGER.info("Démarrage des migrations Flyway sur : " + url);
        
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(url, user, password)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();

            flyway.migrate();
            LOGGER.info("Migrations Flyway terminées avec succès.");
        } catch (Exception e) {
            LOGGER.severe("Échec des migrations Flyway : " + e.getMessage());
            // On laisse l'exception remonter pour empêcher le démarrage en état instable
            throw new RuntimeException("Erreur critique lors de la migration de la base de données", e);
        }
    }
}
