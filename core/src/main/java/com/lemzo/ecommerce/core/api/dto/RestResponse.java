package com.lemzo.ecommerce.core.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.List;

/**
 * Enveloppe standard HATEOAS pour les réponses de l'API.
 */
@Schema(description = "Réponse API unifiée avec liens HATEOAS")
public record RestResponse<T>(
    @Schema(description = "Données de la réponse")
    T data,
    
    @Schema(description = "Liens hypermédias pour la découverte de l'API")
    List<Link> links
) {
    public static <T> RestResponse<T> create(final T data) {
        return new RestResponse<>(data, List.of());
    }

    public static <T> RestResponse<T> create(final T data, final List<Link> links) {
        return new RestResponse<>(data, List.copyOf(links));
    }
}
