package com.lemzo.ecommerce.core.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Structure unifiée des erreurs API.
 */
@Schema(description = "Détails d'une erreur API")
public record ErrorResponse(
    @Schema(description = "Code statut HTTP", example = "400")
    int status,
    
    @Schema(description = "Libellé de l'erreur", example = "Bad Request")
    String error,
    
    @Schema(description = "Message détaillé", example = "Le champ email est requis")
    String message,
    
    @Schema(description = "Horodatage de l'erreur")
    OffsetDateTime timestamp,
    
    @Schema(description = "Détails supplémentaires (ex: erreurs de validation)")
    Map<String, String> details
) {
    public static ErrorResponse create(final int status, final String error, final String message) {
        return new ErrorResponse(status, error, message, OffsetDateTime.now(), Map.of());
    }

    public static ErrorResponse create(final int status, final String error, final String message, final Map<String, String> details) {
        return new ErrorResponse(status, error, message, OffsetDateTime.now(), details);
    }
}
