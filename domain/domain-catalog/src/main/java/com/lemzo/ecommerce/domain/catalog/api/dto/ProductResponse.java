package com.lemzo.ecommerce.domain.catalog.api.dto;

import com.lemzo.ecommerce.domain.catalog.domain.Product;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse pour un produit.
 */
@Schema(description = "Informations détaillées d'un produit")
public record ProductResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Nom du produit", example = "iPhone 15 Pro")
    String name,
    
    @Schema(description = "Slug unique pour l'URL", example = "iphone-15-pro")
    String slug,
    
    @Schema(description = "Référence stock (SKU)", example = "AAPL-I15P")
    String sku,
    
    @Schema(description = "Description complète")
    String description,
    
    @Schema(description = "Prix unitaire", example = "850000")
    BigDecimal price,
    
    @Schema(description = "ID de la catégorie")
    UUID categoryId,
    
    @Schema(description = "Nom de la catégorie")
    String categoryName,
    
    @Schema(description = "État d'activation")
    boolean active,
    
    @Schema(description = "Attributs dynamiques (taille, couleur, etc.)")
    Map<String, Object> attributes,
    
    @Schema(description = "URL de l'image (présignée si nécessaire)")
    String imageUrl,
    
    @Schema(description = "Nombre de vues", example = "150")
    long viewCount,
    
    @Schema(description = "Poids en kg", example = "0.2")
    BigDecimal weight,
    
    @Schema(description = "Configuration logistique (JSON)")
    Map<String, Object> shippingConfig
) {
    public static ProductResponse from(final Product product) {
        return from(product, product.getImageUrl());
    }

    public static ProductResponse from(final Product product, final String fullImageUrl) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getSku(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory().getId(),
            product.getCategory().getName(),
            product.isActive(),
            Map.copyOf(product.getAttributes()),
            fullImageUrl,
            (long) product.getViewCount(),
            product.getWeight(),
            Map.copyOf(product.getShippingConfig())
        );
    }
}
