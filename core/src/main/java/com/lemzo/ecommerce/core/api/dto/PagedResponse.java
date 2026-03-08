package com.lemzo.ecommerce.core.api.dto;

import jakarta.data.page.Page;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Enveloppe standard pour les réponses paginées.
 * Adapté pour Jakarta Data 1.0 (Page API).
 */
public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        long totalPages,
        long pageNumber,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {
    public PagedResponse {
        content = Collections.unmodifiableList(Optional.ofNullable(content).orElseGet(Collections::emptyList));
    }

    public static <T> PagedResponse<T> from(Page<T> page) {
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
