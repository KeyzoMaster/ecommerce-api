package com.lemzo.ecommerce.domain.inventory.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Requête de mise à jour de stock.
 */
@Schema(description = "Données pour la mise à jour manuelle des stocks")
public record StockUpdateRequest(
    @Schema(description = "Nouvelle quantité en stock", example = "50", required = true)
    @NotNull(message = "La quantité est requise")
    Integer quantity,
    
    @Schema(description = "Seuil d'alerte stock faible", example = "5")
    Integer lowStockThreshold
) {}
