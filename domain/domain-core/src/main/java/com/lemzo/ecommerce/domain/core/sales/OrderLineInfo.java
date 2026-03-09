package com.lemzo.ecommerce.domain.core.sales;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Informations sur une ligne de commande nécessaires pour les calculs transverses.
 */
public record OrderLineInfo(
    UUID productId,
    UUID storeId,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal weight,
    Map<String, Object> shippingConfig
) {}
