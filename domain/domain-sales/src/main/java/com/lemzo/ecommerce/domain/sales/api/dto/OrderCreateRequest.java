package com.lemzo.ecommerce.domain.sales.api.dto;

import com.lemzo.ecommerce.core.domain.shipping.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête de création de commande.
 */
public record OrderCreateRequest(
    @NotBlank(message = "L'adresse de livraison est requise")
    String shippingAddressId,
    
    @NotNull(message = "La méthode de livraison est requise")
    ShippingMethod shippingMethod,
    
    @NotBlank(message = "La passerelle de paiement est requise")
    String paymentProvider,
    
    String couponCode
) {}
