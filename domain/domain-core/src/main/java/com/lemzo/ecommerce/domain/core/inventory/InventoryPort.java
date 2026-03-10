package com.lemzo.ecommerce.domain.core.inventory;

import java.util.UUID;

/**
 * Port pour la gestion des stocks.
 */
public interface InventoryPort {
    boolean isAvailable(UUID productId, int quantity);
    void increaseStock(UUID productId, int quantity);
    void decreaseStock(UUID productId, int quantity);
}
