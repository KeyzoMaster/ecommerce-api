package com.lemzo.ecommerce.domain.inventory.api.dto;

import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import java.util.UUID;

/**
 * Réponse pour l'état d'un stock.
 */
public record StockResponse(
    UUID productId,
    int quantity,
    int lowStockThreshold
) {
    public static StockResponse from(final Stock stock) {
        return new StockResponse(
            stock.getProductId(),
            stock.getQuantity(),
            stock.getLowStockThreshold()
        );
    }
}
