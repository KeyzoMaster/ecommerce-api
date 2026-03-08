package com.lemzo.ecommerce.domain.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Requête de mise à jour du stock")
public record StockUpdateRequest(
    @NotNull
    @Schema(description = "Variation de la quantité (positif pour ajout, négatif pour retrait)", example = "10")
    int quantityChange
) {}
