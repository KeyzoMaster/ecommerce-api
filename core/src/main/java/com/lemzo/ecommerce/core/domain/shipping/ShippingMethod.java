package com.lemzo.ecommerce.core.domain.shipping;

import lombok.Getter;

/**
 * Modes de livraison supportés par la plateforme.
 */
@Getter
public enum ShippingMethod {
    STANDARD("Livraison Standard", 3, 5),
    EXPRESS("Livraison Express", 1, 2),
    RELAY("Point Relais", 4, 7),
    PICKUP("Retrait en magasin", 0, 1);

    private final String label;
    private final int minDays;
    private final int maxDays;

    ShippingMethod(String label, int minDays, int maxDays) {
        this.label = label;
        this.minDays = minDays;
        this.maxDays = maxDays;
    }
}
