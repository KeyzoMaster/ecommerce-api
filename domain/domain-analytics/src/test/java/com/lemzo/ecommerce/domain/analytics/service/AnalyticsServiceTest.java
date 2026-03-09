package com.lemzo.ecommerce.domain.analytics.service;

import com.lemzo.ecommerce.domain.analytics.api.dto.DailySalesStats;
import com.lemzo.ecommerce.domain.analytics.api.dto.TopProductStats;
import com.lemzo.ecommerce.domain.analytics.repository.AnalyticsRepository;
import com.lemzo.ecommerce.core.contract.util.CsvExportPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour AnalyticsService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Unit Tests")
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository repository;

    @Mock
    private CsvExportPort csvExportPort;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("Should correctly calculate dashboard totals")
    void shouldCalculateDashboardTotals() {
        // Arrange
        final var dailyStats = List.of(
                new DailySalesStats(LocalDate.now(), 2, new BigDecimal("1000")),
                new DailySalesStats(LocalDate.now().minusDays(1), 3, new BigDecimal("1500"))
        );
        final var topStats = List.of(new TopProductStats(UUID.randomUUID(), "Prod", 10, new BigDecimal("500")));

        when(repository.getDailySales()).thenReturn(dailyStats);
        when(repository.getTopProducts()).thenReturn(topStats);

        // Act
        final var dashboard = analyticsService.getDashboard();

        // Assert
        assertEquals(new BigDecimal("2500"), dashboard.totalRevenue());
        assertEquals(5, dashboard.totalOrders());
        assertEquals(1, dashboard.topProducts().size());
    }

    @Test
    @DisplayName("Should trigger CSV generation for daily trends")
    void shouldExportDailyTrends() {
        // Arrange
        when(repository.getDailySales()).thenReturn(List.of());
        when(csvExportPort.generateCsv(any(), any(), any())).thenReturn("csv-content");

        // Act
        final String result = analyticsService.exportDailyTrendsCsv();

        // Assert
        assertEquals("csv-content", result);
    }
}
