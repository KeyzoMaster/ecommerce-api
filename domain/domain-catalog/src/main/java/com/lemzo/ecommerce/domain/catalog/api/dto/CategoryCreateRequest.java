package com.lemzo.ecommerce.domain.catalog.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création de catégorie.
 */
@Schema(description = "Données pour la création d'une catégorie")
public record CategoryCreateRequest(
    @Schema(description = "Nom de la catégorie", example = "Électronique", required = true)
    @NotBlank(message = "Le nom est requis")
    String name,
    
    @Schema(description = "Slug unique pour l'URL", example = "electronique", required = true)
    @NotBlank(message = "Le slug est requis")
    String slug,
    
    @Schema(description = "Description de la catégorie")
    String description,
    
    @Schema(description = "ID de la catégorie parente")
    java.util.UUID parentId
) {}
