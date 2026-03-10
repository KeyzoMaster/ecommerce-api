package com.lemzo.ecommerce.domain.analytics.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Statistiques de ventes quotidiennes.
 */
@Schema(description = "Indicateurs de performance pour une journée donnée")
public record DailySalesStats(
    @Schema(description = "Date concernée")
    OffsetDateTime date,
    
    @Schema(description = "Nombre de commandes passées", example = "42")
    long count,
    
    @Schema(description = "Chiffre d'affaires total généré", example = "150000.00")
    BigDecimal revenue
) {}
