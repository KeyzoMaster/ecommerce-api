package com.lemzo.ecommerce.core.api.dto;

import jakarta.data.page.Page;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * Enveloppe HATEOAS pour les résultats paginés.
 */
@Schema(description = "Réponse API paginée avec liens HATEOAS")
public record PagedRestResponse<T>(
    @Schema(description = "Données paginées")
    PagedResponse<T> data,
    
    @Schema(description = "Liens de navigation (next, prev, self)")
    List<Link> links
) {
    public static <T> PagedRestResponse<T> from(Page<T> page, String baseUrl) {
        return from(page, page.content(), baseUrl);
    }

    public static <T, R> PagedRestResponse<R> from(Page<T> page, List<R> mappedContent, String baseUrl) {
        List<Link> links = new ArrayList<>();
        
        // Lien actuel
        links.add(Link.of("self", baseUrl + "?page=" + (page.pageRequest().page() - 1), "GET"));
        
        // Lien suivant
        if (page.hasNext()) {
            links.add(Link.of("next", baseUrl + "?page=" + page.pageRequest().page(), "GET"));
        }
        
        // Lien précédent
        if (page.hasPrevious()) {
            links.add(Link.of("prev", baseUrl + "?page=" + (page.pageRequest().page() - 2), "GET"));
        }

        PagedResponse<R> pagedResponse = new PagedResponse<>(
                mappedContent,
                page.hasTotals() ? page.totalElements() : -1L,
                page.hasTotals() ? page.totalPages() : -1L,
                page.pageRequest().page(),
                page.pageRequest().size(),
                page.hasNext(),
                page.hasPrevious()
        );

        return new PagedRestResponse<>(pagedResponse, links);
    }
}
