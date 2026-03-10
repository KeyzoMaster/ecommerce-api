package com.lemzo.ecommerce.core.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Représentation d'un lien HATEOAS.
 */
@Schema(description = "Lien hypermédia HATEOAS")
public record Link(
    @Schema(description = "Relation du lien", example = "self")
    String rel, 
    
    @Schema(description = "URL cible", example = "http://api.local/v1/resource/123")
    String href, 
    
    @Schema(description = "Méthode HTTP", example = "GET")
    String method
) {
    public static Link self(final String href) {
        return new Link("self", href, "GET");
    }

    public static Link create(final String rel, final String href, final String method) {
        return new Link(rel, href, method);
    }
}
