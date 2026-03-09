package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.iam.domain.Store;
import java.util.UUID;

/**
 * Réponse pour une boutique.
 */
public record StoreResponse(
    UUID id,
    String name,
    String slug,
    String description,
    UUID ownerId
) {
    public static StoreResponse from(final Store store) {
        return new StoreResponse(
            store.getId(),
            store.getName(),
            store.getSlug(),
            store.getDescription(),
            store.getOwner().getId()
        );
    }
}
