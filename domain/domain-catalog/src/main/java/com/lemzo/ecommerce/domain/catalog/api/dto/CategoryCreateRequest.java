package com.lemzo.ecommerce.domain.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création de catégorie.
 */
public record CategoryCreateRequest(
    @NotBlank(message = "Le nom est requis")
    String name,
    
    @NotBlank(message = "Le slug est requis")
    String slug,
    
    String description,
    
    java.util.UUID parentId
) {}
