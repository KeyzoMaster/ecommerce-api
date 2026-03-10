package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Requête de création de boutique.
 */
@Schema(description = "Données pour la création d'une boutique")
public record StoreCreateRequest(
    @Schema(description = "Nom de la boutique", example = "Lemzo Store", required = true)
    String name,
    
    @Schema(description = "Slug unique", example = "lemzo-store", required = true)
    String slug,
    
    @Schema(description = "Description optionnelle")
    String description
) {}
