package com.lemzo.ecommerce.domain.sales.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import com.lemzo.ecommerce.domain.core.shipping.ShippingMethod;

/**
 * Réponse pour un mode de livraison.
 */
@Schema(description = "Informations sur un mode de livraison disponible")
public record ShippingMethodResponse(
    @Schema(description = "Identifiant technique", example = "EXPRESS")
    String id,
    
    @Schema(description = "Libellé affichable", example = "Livraison Express (24h)")
    String label,
    
    @Schema(description = "Délai minimum en jours", example = "1")
    int minDays,
    
    @Schema(description = "Délai maximum en jours", example = "2")
    int maxDays
) {
    public static ShippingMethodResponse from(final ShippingMethod method) {
        return new ShippingMethodResponse(
            method.name(),
            method.getLabel(),
            method.getMinDays(),
            method.getMaxDays()
        );
    }
}
