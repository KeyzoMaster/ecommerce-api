package com.lemzo.ecommerce.domain.catalog.repository;

import com.lemzo.ecommerce.domain.catalog.domain.Product;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les produits utilisant Jakarta Data 1.0.
 */
@Repository
public interface ProductRepository extends BasicRepository<Product, UUID> {

    @Find
    Optional<Product> findBySlug(String slug);

    @Find
    Optional<Product> findBySku(String sku);

    @Find
    Page<Product> findByCategoryId(UUID categoryId, PageRequest pageRequest);

    @Find
    Page<Product> findAll(PageRequest pageRequest);

    @Query("from Product p WHERE p.attributes ->> :key = :value")
    Page<Product> findByAttribute(String key, String value, PageRequest pageRequest);

    @Query("from Product p WHERE p.searchVector @@ websearch_to_tsquery('french', :query) " +
           "ORDER BY ts_rank(p.searchVector, websearch_to_tsquery('french', :query)) DESC")
    Page<Product> searchFullText(String query, PageRequest pageRequest);

    @Query("""
        from Product p WHERE 
        (:query IS NULL OR p.searchVector @@ websearch_to_tsquery('french', :query)) AND 
        (:categoryId IS NULL OR p.category.id = :categoryId) AND 
        (:minPrice IS NULL OR p.price >= :minPrice) AND 
        (:maxPrice IS NULL OR p.price <= :maxPrice) AND 
        (:available IS NULL OR p.active = :available) 
        ORDER BY CASE WHEN :query IS NOT NULL THEN ts_rank(p.searchVector, websearch_to_tsquery('french', :query)) ELSE 0 END DESC
    """)
    Page<Product> searchByCriteria(String query, UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean available, PageRequest pageRequest);
}
