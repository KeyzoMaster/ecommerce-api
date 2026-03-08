package com.lemzo.ecommerce.domain.catalog.service;

import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.CategoryRepository;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CatalogService catalogService;

    private UUID categoryId;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = new Category("Electronics", "electronics", "Electronic goods");
        category.setId(categoryId);
    }

    @Test
    @DisplayName("Should successfully create a product")
    void shouldCreateProductSuccessfully() {
        // Arrange
        String name = "Laptop";
        String slug = "laptop";
        String sku = "LAP-001";
        BigDecimal price = new BigDecimal("999.99");
        Map<String, Object> attrs = Map.of("brand", "Dell");
        String imageUrl = "images/laptop.png";
        BigDecimal weight = new BigDecimal("2.5");
        Map<String, Object> shippingConfig = Map.of("allowed_methods", List.of("EXPRESS"));

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.insert(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Product result = catalogService.createProduct(UUID.randomUUID(), name, slug, sku, price, categoryId, attrs, imageUrl, weight, shippingConfig);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(category, result.getCategory());
        assertEquals(attrs, result.getAttributes());
        assertEquals(imageUrl, result.getImageUrl());
        assertEquals(weight, result.getWeight());
        assertEquals(shippingConfig, result.getShippingConfig());
        verify(productRepository).insert(any(Product.class));
    }

    @Test
    @DisplayName("Should successfully update a product partially")
    void shouldUpdateProductPartially() {
        // Arrange
        UUID id = UUID.randomUUID();
        Product existing = new Product("Old Name", "old-slug", "OLD-001", BigDecimal.TEN, category);
        existing.setId(id);

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Product result = catalogService.updateProduct(id, "New Name", null, null, null, new BigDecimal("15.00"), null, true, null, "new-img.jpg", new BigDecimal("1.2"), null);

        // Assert
        assertEquals("New Name", result.getName());
        assertEquals("old-slug", result.getSlug()); 
        assertEquals(new BigDecimal("15.00"), result.getPrice());
        assertEquals("new-img.jpg", result.getImageUrl());
        assertEquals(new BigDecimal("1.2"), result.getWeight());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should fail to create product if category is missing")
    void shouldFailIfCategoryNotFound() {
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            catalogService.createProduct(UUID.randomUUID(), "Name", "slug", "sku", BigDecimal.ONE, UUID.randomUUID(), Map.of(), null, BigDecimal.ZERO, null)
        );    }

    @Test
    @DisplayName("Should successfully create a category")
    void shouldCreateCategorySuccessfully() {
        String name = "Mobiles";
        String slug = "mobiles";
        String desc = "Smartphones and tablets";

        when(categoryRepository.insert(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        Category result = catalogService.createCategory(name, slug, desc, null);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(slug, result.getSlug());
        verify(categoryRepository).insert(any(Category.class));
    }

    @Test
    @DisplayName("Should find product by slug")
    void shouldFindProductBySlug() {
        String slug = "test-product";
        Product product = new Product("Test", slug, "SKU", BigDecimal.ONE, category);
        
        when(productRepository.findBySlug(slug)).thenReturn(Optional.of(product));

        Optional<Product> result = catalogService.getProductBySlug(slug);

        assertTrue(result.isPresent());
        assertEquals(slug, result.get().getSlug());
    }
}
