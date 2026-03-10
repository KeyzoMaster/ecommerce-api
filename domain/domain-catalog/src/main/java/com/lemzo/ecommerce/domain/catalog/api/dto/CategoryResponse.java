package com.lemzo.ecommerce.domain.catalog.api.dto;

import com.lemzo.ecommerce.domain.catalog.domain.Category;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse pour une catégorie.
 */
@Schema(description = "Informations d'une catégorie du catalogue")
public record CategoryResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Nom de la catégorie", example = "Électronique")
    String name,
    
    @Schema(description = "Slug unique", example = "electronique")
    String slug,
    
    @Schema(description = "Description de la catégorie")
    String description,
    
    @Schema(description = "ID de la catégorie parente (si applicable)")
    UUID parentId
) {
    public static CategoryResponse from(final Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getDescription(),
            Optional.ofNullable(category.getParent())
                    .map(Category::getId)
                    .orElse(null)
        );
    }
}
