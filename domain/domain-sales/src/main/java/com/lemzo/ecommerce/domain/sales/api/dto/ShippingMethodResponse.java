package com.lemzo.ecommerce.domain.sales.api.dto;

import com.lemzo.ecommerce.core.domain.shipping.ShippingMethod;
import java.math.BigDecimal;

/**
 * Réponse pour une méthode de livraison avec son coût calculé.
 */
public record ShippingMethodResponse(
    ShippingMethod method,
    String label,
    BigDecimal cost,
    int minDays,
    int maxDays
) {}
