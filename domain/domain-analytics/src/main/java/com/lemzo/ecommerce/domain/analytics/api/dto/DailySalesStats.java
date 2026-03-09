package com.lemzo.ecommerce.domain.analytics.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Statistiques de ventes quotidiennes.
 */
public record DailySalesStats(
    LocalDate date,
    long count,
    BigDecimal revenue
) {}
