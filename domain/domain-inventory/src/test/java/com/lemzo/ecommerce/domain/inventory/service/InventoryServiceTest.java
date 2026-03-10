package com.lemzo.ecommerce.domain.inventory.service;

import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import com.lemzo.ecommerce.domain.inventory.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Should correctly set stock")
    void shouldSetStock() {
        // Arrange
        final UUID productId = UUID.randomUUID();
        final Stock stock = new Stock(productId, 10);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.update(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        inventoryService.setStock(productId, 50, 5);

        // Assert
        assertEquals(50, stock.getQuantity());
        assertEquals(5, stock.getLowStockThreshold());
        verify(stockRepository).update(any(Stock.class));
    }

    @Test
    @DisplayName("Should correctly decrease stock")
    void shouldDecreaseStock() {
        // Arrange
        final UUID productId = UUID.randomUUID();
        final Stock stock = new Stock(productId, 10);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        // Act
        inventoryService.updateStock(productId, -3);

        // Assert
        assertEquals(7, stock.getQuantity());
        verify(stockRepository).update(any(Stock.class));
    }
}
