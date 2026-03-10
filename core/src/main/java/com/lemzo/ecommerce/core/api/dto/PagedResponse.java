package com.lemzo.ecommerce.core.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.data.page.Page;
import java.util.List;

/**
 * Enveloppe standard pour les réponses paginées.
 */
@Schema(description = "Données de pagination")
public record PagedResponse<T>(
    @Schema(description = "Éléments de la page actuelle")
    List<T> content,
    
    @Schema(description = "Nombre total d'éléments sur toutes les pages", example = "100")
    long totalElements,
    
    @Schema(description = "Nombre total de pages", example = "5")
    long totalPages,
    
    @Schema(description = "Numéro de la page actuelle", example = "1")
    long pageNumber,
    
    @Schema(description = "Taille de la page", example = "20")
    int pageSize,
    
    @Schema(description = "Indique s'il existe une page suivante")
    boolean hasNext,
    
    @Schema(description = "Indique s'il existe une page précédente")
    boolean hasPrevious
) {
    public static <T> PagedResponse<T> from(final Page<T> page) {
        return new PagedResponse<>(
                page.content(),
                page.totalElements(),
                page.totalPages(),
                page.pageRequest().page(),
                page.pageRequest().size(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
