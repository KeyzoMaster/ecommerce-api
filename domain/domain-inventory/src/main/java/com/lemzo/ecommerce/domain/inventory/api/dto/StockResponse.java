package com.lemzo.ecommerce.domain.inventory.api.dto;

import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "État du stock pour un produit")
public record StockResponse(
    @Schema(description = "ID du produit")
    UUID productId,
    
    @Schema(description = "Quantité actuellement en stock", example = "42")
    int quantity,
    
    @Schema(description = "Seuil d'alerte de stock bas", example = "5")
    int lowStockThreshold
) {
    public static StockResponse from(Stock stock) {
        return new StockResponse(
            stock.getProductId(),
            stock.getQuantity(),
            stock.getLowStockThreshold()
        );
    }
}
