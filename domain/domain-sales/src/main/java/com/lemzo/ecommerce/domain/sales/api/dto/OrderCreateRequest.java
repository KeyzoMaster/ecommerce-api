package com.lemzo.ecommerce.domain.sales.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import com.lemzo.ecommerce.domain.core.shipping.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête de création de commande.
 */
@Schema(description = "Données pour la finalisation d'une commande")
public record OrderCreateRequest(
    @Schema(description = "ID technique de l'adresse de livraison", required = true)
    @NotBlank String shippingAddressId,
    
    @Schema(description = "Mode de livraison choisi", required = true)
    @NotNull ShippingMethod shippingMethod,
    
    @Schema(description = "Fournisseur de paiement", example = "STRIPE", required = true)
    @NotBlank String paymentProvider,
    
    @Schema(description = "Code coupon éventuel", example = "PROMO2026")
    String couponCode
) {}
