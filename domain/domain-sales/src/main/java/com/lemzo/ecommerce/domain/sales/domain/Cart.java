package com.lemzo.ecommerce.domain.sales.domain;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Représentation immuable du panier utilisateur.
 */
@Schema(description = "Panier utilisateur complet")
public record Cart(
    @Schema(description = "ID de l'utilisateur propriétaire")
    UUID userId,
    
    @Schema(description = "Liste des articles")
    List<CartItem> items
) {
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
