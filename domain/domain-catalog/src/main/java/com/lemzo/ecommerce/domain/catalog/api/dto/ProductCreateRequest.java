package com.lemzo.ecommerce.domain.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Requête de création de produit.
 */
public record ProductCreateRequest(
    @NotBlank(message = "Le nom est requis")
    String name,
    
    @NotBlank(message = "Le slug est requis")
    String slug,
    
    @NotBlank(message = "Le SKU est requis")
    String sku,
    
    String description,
    
    @NotNull(message = "Le prix est requis")
    @Positive(message = "Le prix doit être positif")
    BigDecimal price,
    
    @NotNull(message = "La catégorie est requise")
    UUID categoryId,
    
    Map<String, Object> attributes,
    String imageUrl,
    BigDecimal weight,
    Map<String, Object> shippingConfig
) {}
