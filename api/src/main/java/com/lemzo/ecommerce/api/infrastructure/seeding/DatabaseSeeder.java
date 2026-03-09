package com.lemzo.ecommerce.api.infrastructure.seeding;

import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Orchestrateur déclenchant le seeding.
 * Gère l'exécution unique et la vérification du profil.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class DatabaseSeeder {

    private static final Logger LOGGER = Logger.getLogger(DatabaseSeeder.class.getName());
    private static final AtomicBoolean SEEDED = new AtomicBoolean(false);

    private final Instance<DataSeeder> seeders;

    /**
     * Exécute le seeding si non déjà fait et si autorisé.
     * @return true si le seeding a été exécuté, false sinon.
     */
    public boolean run() {
        final Config config = ConfigProvider.getConfig();
        final String activeProfile = config.getOptionalValue("mp.config.profile", String.class).orElse("dev");

        if (!"dev".equalsIgnoreCase(activeProfile)) {
            LOGGER.warning("Tentative de seeding en dehors du profil 'dev'. Annulation.");
            return false;
        }

        if (SEEDED.getAndSet(true)) {
            LOGGER.info("Le seeding a déjà été exécuté pour cette session.");
            return false;
        }

        LOGGER.info(() -> "Démarrage du seeding manuel pour le profil : " + activeProfile);

        Optional.ofNullable(seeders)
                .filter(s -> !s.isUnsatisfied())
                .ifPresentOrElse(
                    s -> s.stream()
                            .sorted(Comparator.comparingInt(DataSeeder::priority))
                            .forEach(seeder -> {
                                LOGGER.info(() -> "Exécution du seeder : " + seeder.getClass().getSimpleName());
                                seeder.seed();
                            }),
                    () -> LOGGER.warning("Aucun seeder trouvé.")
                );

        LOGGER.info("Seeding terminé.");
        return true;
    }
}
