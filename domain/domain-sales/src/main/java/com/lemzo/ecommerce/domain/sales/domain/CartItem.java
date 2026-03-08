package com.lemzo.ecommerce.domain.sales.domain;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Représentation immuable d'un article dans le panier.
 */
@Schema(description = "Article dans le panier")
public record CartItem(
    @Schema(description = "ID du produit")
    UUID productId,
    
    @Schema(description = "Nom du produit")
    String productName,
    
    @Schema(description = "Quantité", example = "2")
    int quantity,
    
    @Schema(description = "Prix unitaire au moment de l'ajout")
    BigDecimal unitPrice
) {
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
