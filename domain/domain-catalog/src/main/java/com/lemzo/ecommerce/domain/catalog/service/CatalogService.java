package com.lemzo.ecommerce.domain.catalog.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.CategoryRepository;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.data.Order;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion du catalogue produits.
 */
@ApplicationScoped
public class CatalogService {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private CategoryRepository categoryRepository;

    @Transactional
    @Audit(action = "PRODUCT_CREATE")
    public Product createProduct(UUID storeId, String name, String slug, String sku, BigDecimal price, UUID categoryId, 
                               Map<String, Object> attributes, String imageUrl, BigDecimal weight, 
                               Map<String, Object> shippingConfig) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée: " + categoryId));

        Product product = new Product(name, slug, sku, price, category);
        product.setStoreId(storeId);
        product.setAttributes(attributes);
        product.setImageUrl(imageUrl);
        product.setWeight(Optional.ofNullable(weight).orElse(BigDecimal.ZERO));
        product.setShippingConfig(Optional.ofNullable(shippingConfig).orElse(Map.of()));
        
        return productRepository.insert(product);
    }

    @Transactional
    @Audit(action = "PRODUCT_UPDATE")
    public Product updateProduct(UUID id, String name, String slug, String sku, String description, 
                               BigDecimal price, UUID categoryId, Boolean active, Map<String, Object> attributes, 
                               String imageUrl, BigDecimal weight, Map<String, Object> shippingConfig) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + id));

        Optional.ofNullable(name).ifPresent(product::setName);
        Optional.ofNullable(slug).ifPresent(product::setSlug);
        Optional.ofNullable(sku).ifPresent(product::setSku);
        Optional.ofNullable(description).ifPresent(product::setDescription);
        Optional.ofNullable(price).ifPresent(product::setPrice);
        Optional.ofNullable(active).ifPresent(product::setActive);
        Optional.ofNullable(attributes).ifPresent(product::setAttributes);
        Optional.ofNullable(imageUrl).ifPresent(product::setImageUrl);
        Optional.ofNullable(weight).ifPresent(product::setWeight);
        Optional.ofNullable(shippingConfig).ifPresent(product::setShippingConfig);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée: " + categoryId));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    public Page<Product> searchProducts(PageRequest pageRequest) {
        return productRepository.findAll(pageRequest);
    }

    public Page<Product> searchProductsTextual(String query, PageRequest pageRequest) {
        return productRepository.searchFullText(query, pageRequest);
    }

    public Page<Product> searchByCriteria(String query, UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean available, PageRequest pageRequest, Order<Product> order) {
        return productRepository.searchByCriteria(query, categoryId, minPrice, maxPrice, available, pageRequest, order);
    }

    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    @Transactional
    @Audit(action = "PRODUCT_IMAGE_UPDATE")
    public void updateProductImage(UUID productId, String imageUrl) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setImageUrl(imageUrl);
            productRepository.save(p);
        });
    }

    @Transactional
    public void incrementViewCount(UUID productId) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setViewCount(p.getViewCount() + 1);
            productRepository.save(p);
        });
    }

    public Page<Product> getProductsByCategory(UUID categoryId, PageRequest pageRequest) {
        return productRepository.findByCategoryId(categoryId, pageRequest);
    }

    @Transactional
    @Audit(action = "CATEGORY_CREATE")
    public Category createCategory(String name, String slug, String description, UUID parentId) {
        Category category = new Category(name, slug, description);
        
        Optional.ofNullable(parentId)
                .flatMap(categoryRepository::findById)
                .ifPresent(category::setParent);

        return categoryRepository.insert(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(UUID id) {
        return categoryRepository.findById(id);
    }
}
