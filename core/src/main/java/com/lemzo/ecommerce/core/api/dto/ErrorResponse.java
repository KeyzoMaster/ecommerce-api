package com.lemzo.ecommerce.core.api.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Structure unifiée des erreurs API.
 * Utilisant les Records Java 25.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        Map<String, String> details
) {
    public ErrorResponse {
        details = Collections.unmodifiableMap(Optional.ofNullable(details).orElseGet(Collections::emptyMap));
    }

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, LocalDateTime.now(), Map.of());
    }

    public static ErrorResponse of(int status, String error, String message, Map<String, String> details) {
        return new ErrorResponse(status, error, message, LocalDateTime.now(), details);
    }
}
