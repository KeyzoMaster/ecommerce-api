package com.lemzo.ecommerce.core.api.seeding;

/**
 * Contrat pour les composants de seeding de données.
 */
@FunctionalInterface
public interface DataSeeder {
    /**
     * Exécute le seeding.
     */
    void seed();

    /**
     * Priorité d'exécution (plus bas = premier).
     */
    default int priority() {
        return 10;
    }
}
