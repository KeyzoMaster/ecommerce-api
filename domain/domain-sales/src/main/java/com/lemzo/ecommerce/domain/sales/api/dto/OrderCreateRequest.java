package com.lemzo.ecommerce.domain.sales.api.dto;

import com.lemzo.ecommerce.core.domain.Address;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Requête pour la création d'une commande")
public record OrderCreateRequest(
    @NotEmpty 
    @Schema(description = "Liste des articles de la commande")
    List<OrderItemRequest> items,

    @NotNull
    @Schema(description = "Passerelle de paiement (ex: stripe, paypal)", example = "stripe")
    String paymentProvider,

    @Schema(description = "Mode de livraison choisi", example = "EXPRESS")
    String shippingMethod,

    @Schema(description = "Code promo optionnel", example = "SOLDE2026")
    String couponCode,

    @NotNull
    @Schema(description = "Adresse de livraison")
    Address shippingAddress
) {
    public record OrderItemRequest(
        @NotNull @Schema(description = "ID du produit") UUID productId,
        @NotNull @Schema(description = "Quantité", example = "1") int quantity
    ) {}
}
