package com.lemzo.ecommerce.domain.catalog.api.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Requête de mise à jour de produit.
 */
public record ProductUpdateRequest(
    String name,
    String slug,
    String sku,
    String description,
    BigDecimal price,
    UUID categoryId,
    Boolean active,
    Map<String, Object> attributes,
    String imageUrl,
    BigDecimal weight,
    Map<String, Object> shippingConfig
) {}
