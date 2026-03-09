package com.lemzo.ecommerce.util.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour CsvExportUtil.
 */
@DisplayName("CsvExportUtil Unit Tests")
class CsvExportUtilTest {

    private final CsvExportUtil csvExportUtil = new CsvExportUtil();

    @Test
    @DisplayName("Should generate valid CSV string with headers and data")
    void shouldGenerateCsv() {
        // Arrange
        final List<String> headers = List.of("ID", "Name", "Price");
        final List<Item> data = List.of(
                new Item("1", "Product 1", "10.5"),
                new Item("2", "Product 2, with comma", "20.0")
        );

        // Act
        final String csv = csvExportUtil.generateCsv(headers, data, item -> List.of(item.id, item.name, item.price));

        // Assert
        assertNotNull(csv);
        assertTrue(csv.startsWith("ID,Name,Price"));
        assertTrue(csv.contains("Product 1"));
        // Vérification de l'échappement pour la virgule
        assertTrue(csv.contains("\"Product 2, with comma\""));
    }

    private record Item(String id, String name, String price) {}
}
