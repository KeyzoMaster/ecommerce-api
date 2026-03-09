package com.lemzo.ecommerce.core.api.dto;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour PagedRestResponse.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PagedRestResponse Unit Tests")
class PagedRestResponseTest {

    @Mock
    private Page<String> page;

    @Test
    @DisplayName("Should generate navigation links correctly")
    void shouldGenerateNavigationLinks() {
        // Arrange
        final String baseUrl = "http://api.local/items";
        final List<String> content = List.of("item1", "item2");
        
        when(page.content()).thenReturn(content);
        when(page.pageRequest()).thenReturn(PageRequest.ofPage(2, 10, true)); // Page 2
        when(page.hasNext()).thenReturn(true);
        when(page.hasPrevious()).thenReturn(true);
        when(page.hasTotals()).thenReturn(true);
        when(page.totalElements()).thenReturn(100L);
        when(page.totalPages()).thenReturn(10L);

        // Act
        final PagedRestResponse<String> response = PagedRestResponse.from(page, baseUrl);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.links().size());
        
        assertTrue(response.links().stream().anyMatch(l -> l.rel().equals("self") && l.href().contains("page=1")));
        assertTrue(response.links().stream().anyMatch(l -> l.rel().equals("next") && l.href().contains("page=2")));
        assertTrue(response.links().stream().anyMatch(l -> l.rel().equals("prev") && l.href().contains("page=0")));
    }

    @Test
    @DisplayName("Should handle first page without previous link")
    void shouldHandleFirstPage() {
        // Arrange
        when(page.content()).thenReturn(List.of());
        when(page.pageRequest()).thenReturn(PageRequest.ofPage(1, 10, true));
        when(page.hasNext()).thenReturn(false);
        when(page.hasPrevious()).thenReturn(false);
        when(page.hasTotals()).thenReturn(false);

        // Act
        final PagedRestResponse<String> response = PagedRestResponse.from(page, "url");

        // Assert
        assertEquals(1, response.links().size()); // Seulement self
        assertEquals("self", response.links().get(0).rel());
    }
}
