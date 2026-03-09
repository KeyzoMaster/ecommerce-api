package com.lemzo.ecommerce.domain.sales.api.dto;

import com.lemzo.ecommerce.domain.core.shipping.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête de création de commande.
 */
public record OrderCreateRequest(
    @NotBlank String shippingAddressId,
    @NotNull ShippingMethod shippingMethod,
    @NotBlank String paymentProvider,
    String couponCode
) {}
