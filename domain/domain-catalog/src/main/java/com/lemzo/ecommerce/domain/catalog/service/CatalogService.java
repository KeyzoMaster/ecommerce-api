package com.lemzo.ecommerce.domain.catalog.service;

import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.domain.core.catalog.CatalogPort;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.domain.CatalogFactory;
import com.lemzo.ecommerce.domain.catalog.repository.CategoryRepository;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
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
 * Service pour les opérations catalogue.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class CatalogService implements CatalogPort {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Optional<Product> findProductById(final UUID productId) {
        return productRepository.findById(productId);
    }

    @Transactional
    @Audit(action = "PRODUCT_CREATE")
    @HasPermission(resource = ResourceType.CATALOG, action = PbacAction.CREATE)
    public Product createProduct(final String name, final String slug, final String sku, 
                                 final BigDecimal price, final UUID categoryId, final UUID storeId,
                                 final Map<String, Object> attributes, final String imageUrl,
                                 final BigDecimal weight, final Map<String, Object> shippingConfig) {
        
        final Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));

        final Product product = CatalogFactory.createProduct(name, slug, sku, price, category);
        product.setStoreId(storeId);
        product.setAttributes(Optional.ofNullable(attributes).orElse(Map.of()));
        product.setImageUrl(imageUrl);
        product.setWeight(Optional.ofNullable(weight).orElse(BigDecimal.ZERO));
        product.setShippingConfig(Optional.ofNullable(shippingConfig).orElse(Map.of()));

        return productRepository.insert(product);
    }

    @Transactional
    @Audit(action = "PRODUCT_UPDATE")
    public Product updateProduct(final UUID id, final String name, final String slug, final String sku,
                                 final String description, final BigDecimal price, final UUID categoryId,
                                 final Boolean active, final Map<String, Object> attributes,
                                 final String imageUrl, final BigDecimal weight, final Map<String, Object> shippingConfig) {
        
        final Product product = productRepository.findById(id)
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

        Optional.ofNullable(categoryId)
                .flatMap(categoryRepository::findById)
                .ifPresent(product::setCategory);

        return productRepository.update(product);
    }

    public Page<Product> findAll(final PageRequest pageRequest) {
        return productRepository.findAll(pageRequest);
    }

    public Page<Product> search(final String query, final PageRequest pageRequest) {
        if (query == null || query.isBlank()) {
            return findAll(pageRequest);
        }
        return productRepository.findByNameLike("%" + query + "%", pageRequest);
    }

    public Page<Product> filter(final String query, final UUID categoryId, final BigDecimal minPrice,
                                final BigDecimal maxPrice, final Boolean available, final PageRequest pageRequest) {
        return search(query, pageRequest);
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
                    productRepository.update(p);
                });
    }

    @Transactional
    public void incrementViewCount(final UUID productId) {
        productRepository.findById(productId)
                .ifPresent(p -> {
                    p.setViewCount(p.getViewCount() + 1);
                    productRepository.update(p);
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
        final Category category = CatalogFactory.createCategory(name, slug, description);
        
        Optional.ofNullable(parentId)
                .flatMap(categoryRepository::findById)
                .ifPresent(category::setParent);

        return categoryRepository.insert(category);
    }

    public Optional<Category> findCategoryById(final UUID id) {
        return categoryRepository.findById(id);
    }
}
