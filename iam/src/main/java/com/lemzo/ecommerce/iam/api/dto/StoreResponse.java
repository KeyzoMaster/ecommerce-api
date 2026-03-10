package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.iam.domain.Store;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse pour une boutique.
 */
@Schema(description = "Informations d'une boutique")
public record StoreResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Nom de la boutique", example = "Lemzo Store")
    String name,
    
    @Schema(description = "Slug unique pour l'URL", example = "lemzo-store")
    String slug,
    
    @Schema(description = "Description de la boutique", example = "Vente de matériel informatique")
    String description,
    
    @Schema(description = "Identifiant du propriétaire")
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
