package com.lemzo.ecommerce.domain.catalog.api.dto;

import com.lemzo.ecommerce.domain.catalog.domain.Product;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Réponse pour un produit.
 */
public record ProductResponse(
    UUID id,
    String name,
    String slug,
    String sku,
    String description,
    BigDecimal price,
    UUID categoryId,
    String categoryName,
    boolean active,
    Map<String, Object> attributes,
    String imageUrl,
    long viewCount,
    BigDecimal weight,
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
