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
import java.time.OffsetDateTime;
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
        final var now = OffsetDateTime.now();
        final List<Object[]> dailyRows = new java.util.ArrayList<>();
        dailyRows.add(new Object[]{ java.sql.Timestamp.from(now.toInstant()), 2L, new BigDecimal("1000") });
        dailyRows.add(new Object[]{ java.sql.Timestamp.from(now.minusDays(1).toInstant()), 3L, new BigDecimal("1500") });

        final List<Object[]> topRows = new java.util.ArrayList<>();
        topRows.add(new Object[]{ UUID.randomUUID(), "Prod", 10L, new BigDecimal("500") });

        when(repository.getDailySalesRaw()).thenReturn(dailyRows);
        when(repository.getTopProductsRaw()).thenReturn(topRows);

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
        when(repository.getDailySalesRaw()).thenReturn(List.of());
        when(csvExportPort.generateCsv(any(), any(), any())).thenReturn("csv-content");

        // Act
        final String result = analyticsService.exportDailyTrendsCsv();

        // Assert
        assertEquals("csv-content", result);
    }
}
