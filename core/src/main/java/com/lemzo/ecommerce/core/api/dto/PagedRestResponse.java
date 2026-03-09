package com.lemzo.ecommerce.core.api.dto;

import jakarta.data.page.Page;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public static <T> PagedRestResponse<T> from(final Page<T> page, final String baseUrl) {
        return from(page, page.content(), baseUrl);
    }

    public static <T, R> PagedRestResponse<R> from(final Page<T> page, final List<R> mappedContent, final String baseUrl) {
        final List<Link> navigationLinks = new ArrayList<>();
        
        navigationLinks.add(Link.create("self", baseUrl + "?page=" + (page.pageRequest().page() - 1), "GET"));
        
        Optional.of(page)
                .filter(Page::hasNext)
                .ifPresent(p -> navigationLinks.add(Link.create("next", baseUrl + "?page=" + p.pageRequest().page(), "GET")));
        
        Optional.of(page)
                .filter(Page::hasPrevious)
                .ifPresent(p -> navigationLinks.add(Link.create("prev", baseUrl + "?page=" + (p.pageRequest().page() - 2), "GET")));

        final PagedResponse<R> pagedResponse = new PagedResponse<>(
                mappedContent,
                page.hasTotals() ? page.totalElements() : -1L,
                page.hasTotals() ? page.totalPages() : -1L,
                page.pageRequest().page(),
                page.pageRequest().size(),
                page.hasNext(),
                page.hasPrevious()
        );

        return new PagedRestResponse<>(pagedResponse, List.copyOf(navigationLinks));
    }
}
