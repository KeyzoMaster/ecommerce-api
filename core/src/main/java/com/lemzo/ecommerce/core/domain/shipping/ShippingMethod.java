package com.lemzo.ecommerce.core.domain.shipping;

import lombok.Getter;

/**
 * Modes de livraison supportés.
 */
@Getter
public enum ShippingMethod {
    STANDARD("Livraison Standard", 3, 5),
    EXPRESS("Livraison Express", 1, 2);

    private final String label;
    private final int minDays;
    private final int maxDays;

    ShippingMethod(final String label, final int minDays, final int maxDays) {
        this.label = label;
        this.minDays = minDays;
        this.maxDays = maxDays;
    }
}
