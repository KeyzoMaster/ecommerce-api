package com.lemzo.ecommerce.domain.catalog.api.dto;

import com.lemzo.ecommerce.domain.catalog.domain.Product;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Détails d'un produit")
public record ProductResponse(
    @Schema(description = "Identifiant unique du produit")
    UUID id,
    
    @Schema(description = "Nom du produit", example = "iPhone 16 Pro")
    String name,
    
    @Schema(description = "Slug pour les URLs", example = "iphone-16-pro")
    String slug,
    
    @Schema(description = "Référence stock (SKU)", example = "APPLE-IPH16P-256")
    String sku,
    
    @Schema(description = "Prix unitaire", example = "1199.99")
    BigDecimal price,
    
    @Schema(description = "Devise", example = "EUR")
    String currency,
    
    @Schema(description = "Attributs dynamiques (JSONB)", example = "{\"couleur\": \"Titane\", \"stockage\": \"256GB\"}")
    Map<String, Object> attributes,
    
    @Schema(description = "URL de l'image principale", example = "https://storage.sicoft.local/products/iphone.jpg")
    String imageUrl,

    @Schema(description = "Poids du produit en kg", example = "1.5")
    BigDecimal weight,

    @Schema(description = "Configuration livraison (méthodes autorisées, etc.)")
    Map<String, Object> shippingConfig
) {
    public static ProductResponse from(Product product) {
        return from(product, product.getImageUrl());
    }

    public static ProductResponse from(Product product, String fullImageUrl) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getSku(),
            product.getPrice(),
            product.getCurrency(),
            product.getAttributes(),
            fullImageUrl,
            product.getWeight(),
            product.getShippingConfig()
        );
    }
}
