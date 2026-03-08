package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Schema(description = "Requête pour l'enregistrement d'un moyen de paiement")
public record PaymentMethodRequest(
    @NotBlank @Schema(description = "Type de paiement", example = "WAVE")
    String type,
    
    @Schema(description = "Données spécifiques (JSON)", example = "{\"phone\": \"771234567\"}")
    Map<String, Object> details
) {}
