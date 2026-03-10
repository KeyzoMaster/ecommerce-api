package com.lemzo.ecommerce.domain.catalog.service;

import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.entity.AbstractEntity;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.CategoryRepository;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
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
@DisplayName("CatalogService Unit Tests")
class CatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CatalogService catalogService;

    // --- PRODUCT TESTS ---

    @Test
    @DisplayName("Should successfully create a product")
    void shouldCreateProductSuccessfully() throws Exception {
        final UUID categoryId = UUID.randomUUID();
        final UUID storeId = UUID.randomUUID();
        final Category category = new Category("Cat", "cat", "desc");

        final var idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(category, categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.insert(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        final Product result = catalogService.createProduct(
                "iPhone", "iphone", "SKU1", new BigDecimal("100"),
                categoryId, storeId, Map.of(), null, BigDecimal.ZERO, Map.of());

        assertNotNull(result);
        assertEquals("iPhone", result.getName());
        assertEquals(storeId, result.getStoreId());
        assertEquals(category, result.getCategory());
        verify(productRepository).insert(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating product with invalid category")
    void shouldThrowWhenCreatingProductWithInvalidCategory() {
        final UUID categoryId = UUID.randomUUID();
        final UUID storeId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            catalogService.createProduct("iPhone", "iphone", "SKU1", new BigDecimal("100"), categoryId, storeId, Map.of(), null, BigDecimal.ZERO, Map.of())
        );

        verify(productRepository, never()).insert(any(Product.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() {
        final UUID productId = UUID.randomUUID();
        final Category category = new Category("Cat", "cat", "desc");
        final Product product = new Product("iPhone", "iphone", "SKU1", new BigDecimal("100"), category);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.update(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        final Product result = catalogService.updateProduct(
                productId, "iPhone 15", "iphone-15", "SKU2", "New Desc", 
                new BigDecimal("200"), null, false, Map.of("color", "red"), 
                "http://img", new BigDecimal("1.5"), Map.of());

        assertEquals("iPhone 15", result.getName());
        assertEquals("iphone-15", result.getSlug());
        assertEquals(new BigDecimal("200"), result.getPrice());
        assertFalse(result.isActive());
        assertEquals("http://img", result.getImageUrl());
        verify(productRepository).update(any(Product.class));
    }

    @Test
    @DisplayName("Should update product image url")
    void shouldUpdateImageUrl() {
        final UUID productId = UUID.randomUUID();
        final Category category = new Category("Cat", "cat", "desc");
        final Product product = new Product("iPhone", "iphone", "SKU1", new BigDecimal("100"), category);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        catalogService.updateImageUrl(productId, "http://new-image.png");
        
        assertEquals("http://new-image.png", product.getImageUrl());
        verify(productRepository).update(product);
    }

    @Test
    @DisplayName("Should increment product view count")
    void shouldIncrementViewCount() {
        final UUID productId = UUID.randomUUID();
        final Category category = new Category("Cat", "cat", "desc");
        final Product product = new Product("iPhone", "iphone", "SKU1", new BigDecimal("100"), category);
        product.setViewCount(5);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        catalogService.incrementViewCount(productId);
        
        assertEquals(6, product.getViewCount());
        verify(productRepository).update(product);
    }

    @Test
    @DisplayName("Should return product by slug")
    void shouldFindProductBySlug() {
        final String slug = "iphone-15";
        final Category category = new Category("Cat", "cat", "desc");
        final Product product = new Product("iPhone", slug, "SKU", BigDecimal.ONE, category);

        when(productRepository.findBySlug(slug)).thenReturn(Optional.of(product));

        final Optional<Product> result = catalogService.findBySlug(slug);

        assertTrue(result.isPresent());
        assertEquals(slug, result.get().getSlug());
    }

    @Test
    @DisplayName("Should search products by query")
    void shouldSearchProductsByQuery() {
        final PageRequest request = PageRequest.ofPage(1, 10, true);
        @SuppressWarnings("unchecked")
        final Page<Product> mockPage = mock(Page.class);
        
        when(productRepository.findByNameLike("%Phone%", request)).thenReturn(mockPage);

        final Page<Product> result = catalogService.search("Phone", request);

        assertEquals(mockPage, result);
        verify(productRepository).findByNameLike("%Phone%", request);
    }

    @Test
    @DisplayName("Should return all products when query is blank")
    void shouldReturnAllWhenQueryIsBlank() {
        final PageRequest request = PageRequest.ofPage(1, 10, true);
        @SuppressWarnings("unchecked")
        final Page<Product> mockPage = mock(Page.class);
        
        when(productRepository.findAll(request)).thenReturn(mockPage);

        final Page<Product> result = catalogService.search("   ", request);

        assertEquals(mockPage, result);
        verify(productRepository).findAll(request);
    }

    // --- CATEGORY TESTS ---

    @Test
    @DisplayName("Should successfully create a category without parent")
    void shouldCreateCategoryWithoutParent() {
        when(categoryRepository.insert(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        final Category result = catalogService.createCategory("Electronics", "electronics", "Desc", null);

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        assertNull(result.getParent());
        verify(categoryRepository).insert(any(Category.class));
    }

    @Test
    @DisplayName("Should successfully create a category with parent")
    void shouldCreateCategoryWithParent() {
        final UUID parentId = UUID.randomUUID();
        final Category parentCategory = new Category("Parent", "parent", "Desc");
        
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.insert(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        final Category result = catalogService.createCategory("Child", "child", "Desc", parentId);

        assertNotNull(result);
        assertEquals("Child", result.getName());
        assertEquals(parentCategory, result.getParent());
        verify(categoryRepository).insert(any(Category.class));
    }

    @Test
    @DisplayName("Should get all categories")
    void shouldGetAllCategories() {
        final List<Category> categories = List.of(new Category("Cat", "cat", "desc"));
        when(categoryRepository.findAll()).thenReturn(categories);

        final List<Category> result = catalogService.getAllCategories();

        assertEquals(1, result.size());
        verify(categoryRepository).findAll();
    }
}