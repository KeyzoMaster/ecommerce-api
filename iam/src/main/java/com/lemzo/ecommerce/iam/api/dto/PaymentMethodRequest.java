package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.Map;

/**
 * Requête d'ajout de moyen de paiement.
 */
@Schema(description = "Données pour l'enregistrement d'un moyen de paiement")
public record PaymentMethodRequest(
    @Schema(description = "Type de paiement", example = "CREDIT_CARD", required = true)
    String type,
    
    @Schema(description = "Détails spécifiques (ex: 4 derniers chiffres)", example = "{\"last4\": \"1234\"}")
    Map<String, Object> details
) {}
