package com.lemzo.ecommerce.iam.api.dto;

import java.util.Map;

/**
 * Requête d'ajout de moyen de paiement.
 */
public record PaymentMethodRequest(
    String type,
    Map<String, Object> details
) {}
