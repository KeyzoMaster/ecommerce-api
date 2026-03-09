package com.lemzo.ecommerce.domain.analytics.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Statistiques pour un produit phare.
 */
public record TopProductStats(
    UUID productId,
    String productName,
    long totalSold,
    BigDecimal totalRevenue
) {}
