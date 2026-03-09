package com.lemzo.ecommerce.domain.catalog.api.dto;

import com.lemzo.ecommerce.domain.catalog.domain.Category;
import java.util.Optional;
import java.util.UUID;

/**
 * Réponse pour une catégorie.
 */
public record CategoryResponse(
    UUID id,
    String name,
    String slug,
    String description,
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
