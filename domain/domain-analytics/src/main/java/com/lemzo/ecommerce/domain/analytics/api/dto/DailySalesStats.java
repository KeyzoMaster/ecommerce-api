package com.lemzo.ecommerce.domain.analytics.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Statistiques de ventes quotidiennes.
 */
public record DailySalesStats(
    OffsetDateTime date,
    long count,
    BigDecimal revenue
) {}
