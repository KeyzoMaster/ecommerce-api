package com.lemzo.ecommerce.domain.inventory.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Requête de mise à jour de stock.
 */
public record StockUpdateRequest(
    @NotNull(message = "La quantité est requise")
    Integer quantity,
    
    Integer lowStockThreshold
) {}
