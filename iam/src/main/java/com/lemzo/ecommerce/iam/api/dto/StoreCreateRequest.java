package com.lemzo.ecommerce.iam.api.dto;

/**
 * Requête de création de boutique.
 */
public record StoreCreateRequest(
    String name,
    String slug,
    String description
) {}
