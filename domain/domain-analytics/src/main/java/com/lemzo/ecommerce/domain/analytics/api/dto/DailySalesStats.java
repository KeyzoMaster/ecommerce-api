package com.lemzo.ecommerce.domain.analytics.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Statistiques de ventes quotidiennes")
public record DailySalesStats(
    @Schema(description = "Date concernée")
    LocalDate date,
    
    @Schema(description = "Nombre de commandes passées")
    long orderCount,
    
    @Schema(description = "Chiffre d'affaires total du jour")
    BigDecimal revenue
) {}
