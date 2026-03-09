package com.lemzo.ecommerce.domain.sales.api.dto;

import com.lemzo.ecommerce.domain.core.shipping.ShippingMethod;

/**
 * Réponse pour un mode de livraison.
 */
public record ShippingMethodResponse(
    String id,
    String label,
    int minDays,
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
