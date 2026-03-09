package com.lemzo.ecommerce.api.infrastructure.concurrency;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;

/**
 * Définition de l'exécuteur de threads virtuels pour l'application.
 * Nouveauté Jakarta Concurrency 3.1 (EE 11).
 */
@ManagedExecutorDefinition(
    name = "java:comp/DefaultManagedExecutorService", // Remplace l'exécuteur par défaut
    virtual = true // Activation des Virtual Threads
)
@ApplicationScoped
public class ConcurrencyConfig {
}
