package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.iam.domain.Store;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Détails d'une boutique")
public record StoreResponse(
    UUID id,
    String name,
    String slug,
    String description,
    UUID ownerId,
    boolean active
) {
    public static StoreResponse from(Store store) {
        return new StoreResponse(
            store.getId(),
            store.getName(),
            store.getSlug(),
            store.getDescription(),
            store.getOwnerId(),
            store.isActive()
        );
    }
}
