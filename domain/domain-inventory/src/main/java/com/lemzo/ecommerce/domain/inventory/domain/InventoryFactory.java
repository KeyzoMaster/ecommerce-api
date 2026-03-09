package com.lemzo.ecommerce.domain.inventory.domain;

import java.util.UUID;

/**
 * Factory pour les entités du domaine Inventory.
 */
public final class InventoryFactory {

    private InventoryFactory() {
        // Classe utilitaire
    }

    /**
     * Crée un stock pour un produit avec un seuil d'alerte par défaut.
     */
    public static Stock createStock(final UUID productId, final int initialQuantity) {
        final Stock stock = new Stock(productId, initialQuantity);
        stock.setLowStockThreshold(5);
        return stock;
    }
}
