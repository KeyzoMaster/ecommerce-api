package com.lemzo.ecommerce.core.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Structure unifiée des erreurs API.
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    LocalDateTime timestamp,
    Map<String, String> details
) {
    public static ErrorResponse create(final int status, final String error, final String message) {
        return new ErrorResponse(status, error, message, LocalDateTime.now(), Map.of());
    }

    public static ErrorResponse create(final int status, final String error, final String message, final Map<String, String> details) {
        return new ErrorResponse(status, error, message, LocalDateTime.now(), details);
    }
}
