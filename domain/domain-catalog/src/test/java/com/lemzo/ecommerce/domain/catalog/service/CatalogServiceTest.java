package com.lemzo.ecommerce.domain.catalog.service;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.CategoryRepository;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogService Unit Tests")
class CatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    @DisplayName("Should successfully create a product")
    void shouldCreateProductSuccessfully() throws Exception {
        // Arrange
        final UUID categoryId = UUID.randomUUID();
        final UUID storeId = UUID.randomUUID(); // Ajout du storeId pour le test
        final Category category = new Category("Cat", "cat", "desc");

        // Réflexion pour définir l'ID car il est généré par la DB normalement
        final var idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(category, categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.insert(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        // Ajout du paramètre storeId dans l'appel à createProduct
        final Product result = catalogService.createProduct(
                "iPhone", "iphone", "SKU1", new BigDecimal("100"),
                categoryId, storeId, Map.of(), null, BigDecimal.ZERO, Map.of());

        // Assert
        assertNotNull(result);
        assertEquals("iPhone", result.getName());
        assertEquals(storeId, result.getStoreId()); // Vérification du storeId
        verify(productRepository).insert(any(Product.class));
    }

    @Test
    @DisplayName("Should find product by slug")
    void shouldFindProductBySlug() {
        // Arrange
        final String slug = "iphone-15";
        final Category category = new Category("Cat", "cat", "desc");
        final Product product = new Product("iPhone", slug, "SKU", BigDecimal.ONE, category);

        when(productRepository.findBySlug(slug)).thenReturn(Optional.of(product));

        // Act
        final Optional<Product> result = catalogService.findBySlug(slug);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(slug, result.get().getSlug());
    }
}