package com.lemzo.ecommerce.core.api.seeding;

/**
 * Interface pour les composants de remplissage de base de données.
 */
public interface DataSeeder {
    /**
     * Exécute le remplissage des données.
     */
    void seed();
    
    /**
     * Définit l'ordre d'exécution (priorité basse = exécuté en premier).
     */
    default int priority() {
        return 0;
    }
}
