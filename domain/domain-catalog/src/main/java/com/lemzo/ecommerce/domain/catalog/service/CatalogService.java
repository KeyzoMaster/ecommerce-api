package com.lemzo.ecommerce.domain.catalog.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.CategoryRepository;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import com.lemzo.ecommerce.core.annotation.Audit;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour la gestion du catalogue produits.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Audit(action = "PRODUCT_CREATE")
    public Product createProduct(final String name, final String slug, final String sku, 
                                 final BigDecimal price, final UUID categoryId, 
                                 final Map<String, Object> attributes, final String imageUrl,
                                 final BigDecimal weight, final Map<String, Object> shippingConfig) {
        
        final var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));

        final var product = new Product(name, slug, sku, price, category);
        product.setAttributes(attributes);
        product.setImageUrl(imageUrl);
        product.setWeight(weight);
        product.setShippingConfig(shippingConfig);

        return productRepository.save(product);
    }

    @Transactional
    @Audit(action = "PRODUCT_UPDATE")
    public Product updateProduct(final UUID id, final String name, final String slug, final String sku,
                                 final String description, final BigDecimal price, final UUID categoryId,
                                 final Boolean active, final Map<String, Object> attributes,
                                 final String imageUrl, final BigDecimal weight, final Map<String, Object> shippingConfig) {
        
        final var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

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
            final var category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    public Page<Product> findAll(final PageRequest pageRequest) {
        return productRepository.findAll(pageRequest);
    }

    public Page<Product> search(final String query, final PageRequest pageRequest) {
        return productRepository.searchFullText(query, pageRequest);
    }

    public Page<Product> filter(final String query, final UUID categoryId, final BigDecimal minPrice,
                                final BigDecimal maxPrice, final Boolean available, final PageRequest pageRequest) {
        return productRepository.searchByCriteria(query, categoryId, minPrice, maxPrice, available, pageRequest);
    }

    public Optional<Product> findBySlug(final String slug) {
        return productRepository.findBySlug(slug);
    }

    public Optional<Product> findById(final UUID id) {
        return productRepository.findById(id);
    }

    @Transactional
    public void updateImageUrl(final UUID productId, final String imageUrl) {
        productRepository.findById(productId)
                .ifPresent(p -> {
                    p.setImageUrl(imageUrl);
                    productRepository.save(p);
                });
    }

    @Transactional
    public void incrementViewCount(final UUID productId) {
        productRepository.findById(productId)
                .ifPresent(p -> {
                    p.setViewCount(p.getViewCount() + 1);
                    productRepository.save(p);
                });
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Page<Product> getProductsByCategory(final UUID categoryId, final PageRequest pageRequest) {
        return productRepository.findByCategoryId(categoryId, pageRequest);
    }

    @Transactional
    @Audit(action = "CATEGORY_CREATE")
    public Category createCategory(final String name, final String slug, final String description, final UUID parentId) {
        final var category = new Category(name, slug, description);
        
        Optional.ofNullable(parentId)
                .flatMap(categoryRepository::findById)
                .ifPresent(category::setParent);

        return categoryRepository.save(category);
    }

    public Optional<Category> findCategoryById(final UUID id) {
        return categoryRepository.findById(id);
    }
}
