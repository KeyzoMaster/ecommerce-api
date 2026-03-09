package com.lemzo.ecommerce.core.api.dto;

import jakarta.data.page.Page;
import java.util.List;

/**
 * Enveloppe standard pour les réponses paginées.
 */
public record PagedResponse<T>(
    List<T> content,
    long totalElements,
    long totalPages,
    long pageNumber,
    int pageSize,
    boolean hasNext,
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
