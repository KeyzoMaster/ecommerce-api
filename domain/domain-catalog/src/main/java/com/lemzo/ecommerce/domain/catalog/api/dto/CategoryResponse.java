package com.lemzo.ecommerce.domain.catalog.api.dto;

import com.lemzo.ecommerce.domain.catalog.domain.Category;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Détails d'une catégorie")
public record CategoryResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Nom", example = "Électronique")
    String name,
    
    @Schema(description = "Slug pour URLs", example = "electronique")
    String slug,
    
    @Schema(description = "Description")
    String description,
    
    @Schema(description = "ID de la catégorie parente")
    UUID parentId
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getDescription(),
            category.getParent() != null ? category.getParent().getId() : null
        );
    }
}
