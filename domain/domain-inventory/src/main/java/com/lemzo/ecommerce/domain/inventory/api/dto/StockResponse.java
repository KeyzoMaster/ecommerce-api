package com.lemzo.ecommerce.domain.inventory.api.dto;

import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse pour l'état d'un stock.
 */
@Schema(description = "Informations sur le stock d'un produit")
public record StockResponse(
    @Schema(description = "Identifiant du produit associé")
    UUID productId,
    
    @Schema(description = "Quantité actuelle disponible", example = "15")
    int quantity,
    
    @Schema(description = "Seuil critique pour alerte", example = "5")
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
