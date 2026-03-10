package com.lemzo.ecommerce.domain.inventory.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import com.lemzo.ecommerce.domain.inventory.repository.StockRepository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Field;
import com.lemzo.ecommerce.core.entity.AbstractEntity;

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
    @DisplayName("Should correctly set stock for existing stock")
    void shouldSetStockForExisting() throws Exception {
        final UUID productId = UUID.randomUUID();
        final Stock stock = new Stock(productId, 10);
        stock.setLowStockThreshold(2);
        
        final Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(stock, UUID.randomUUID());
        
        // Mocking behavior of a saved entity by manually setting ID via reflection or just simulating update
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.update(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        final Stock result = inventoryService.setStock(productId, 50, 5);

        // Assert
        assertEquals(50, result.getQuantity());
        assertEquals(5, result.getLowStockThreshold());
        verify(stockRepository).update(stock);
        verify(stockRepository, never()).insert(any());
    }

    @Test
    @DisplayName("Should correctly set stock for new stock")
    void shouldSetStockForNew() {
        final UUID productId = UUID.randomUUID();
        
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());
        when(stockRepository.insert(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        final Stock result = inventoryService.setStock(productId, 50, 5);

        // Assert
        assertEquals(50, result.getQuantity());
        assertEquals(5, result.getLowStockThreshold());
        verify(stockRepository).insert(any(Stock.class));
        verify(stockRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should return true when stock is available")
    void shouldReturnTrueWhenStockAvailable() {
        final UUID productId = UUID.randomUUID();
        final Stock stock = new Stock(productId, 10);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        assertTrue(inventoryService.isAvailable(productId, 5));
        assertTrue(inventoryService.isAvailable(productId, 10));
    }

    @Test
    @DisplayName("Should return false when stock is not available or missing")
    void shouldReturnFalseWhenStockNotAvailable() {
        final UUID productId = UUID.randomUUID();
        final Stock stock = new Stock(productId, 5);
        
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        assertFalse(inventoryService.isAvailable(productId, 10));

        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());
        assertFalse(inventoryService.isAvailable(productId, 1));
    }

    @Test
    @DisplayName("Should correctly increase stock")
    void shouldIncreaseStock() {
        final UUID productId = UUID.randomUUID();
        final Stock stock = new Stock(productId, 10);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        inventoryService.increaseStock(productId, 5);

        assertEquals(15, stock.getQuantity());
        verify(stockRepository).update(stock);
    }

    @Test
    @DisplayName("Should throw when increasing stock with negative quantity")
    void shouldThrowWhenIncreaseNegative() {
        final UUID productId = UUID.randomUUID();

        assertThrows(BusinessRuleException.class, () -> inventoryService.increaseStock(productId, -5));
        verify(stockRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw when increasing stock of unknown product")
    void shouldThrowWhenIncreaseUnknownProduct() {
        final UUID productId = UUID.randomUUID();
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> inventoryService.increaseStock(productId, 5));
    }

    @Test
    @DisplayName("Should correctly decrease stock")
    void shouldDecreaseStock() {
        final UUID productId = UUID.randomUUID();
        final Stock stock = new Stock(productId, 10);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        inventoryService.decreaseStock(productId, 3);

        assertEquals(7, stock.getQuantity());
        verify(stockRepository).update(stock);
    }

    @Test
    @DisplayName("Should throw when decreasing stock with negative quantity")
    void shouldThrowWhenDecreaseNegative() {
        final UUID productId = UUID.randomUUID();

        assertThrows(BusinessRuleException.class, () -> inventoryService.decreaseStock(productId, -5));
        verify(stockRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw when decreasing stock below zero")
    void shouldThrowWhenDecreaseBelowZero() {
        final UUID productId = UUID.randomUUID();
        final Stock stock = new Stock(productId, 10);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        assertThrows(BusinessRuleException.class, () -> inventoryService.decreaseStock(productId, 15));
        verify(stockRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw when decreasing stock of unknown product")
    void shouldThrowWhenDecreaseUnknownProduct() {
        final UUID productId = UUID.randomUUID();
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> inventoryService.decreaseStock(productId, 5));
    }

    @Test
    @DisplayName("Should list low stocks correctly")
    void shouldListLowStocks() {
        final Stock lowStock = new Stock(UUID.randomUUID(), 2);
        lowStock.setLowStockThreshold(5);
        
        final Stock normalStock = new Stock(UUID.randomUUID(), 10);
        normalStock.setLowStockThreshold(5);

        @SuppressWarnings("unchecked")
        final Page<Stock> mockPage = mock(Page.class);
        when(mockPage.content()).thenReturn(List.of(lowStock, normalStock));
        when(stockRepository.findAll(any(PageRequest.class))).thenReturn(mockPage);

        final List<Stock> result = inventoryService.getLowStocks();

        assertEquals(1, result.size());
        assertEquals(lowStock, result.get(0));
    }
}