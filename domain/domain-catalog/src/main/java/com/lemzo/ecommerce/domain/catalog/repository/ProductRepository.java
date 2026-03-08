package com.lemzo.ecommerce.domain.catalog.repository;

import com.lemzo.ecommerce.domain.catalog.domain.Product;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.Order;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository {

    @Insert
    Product insert(Product product);

    @Save
    Product save(Product product);

    @Find
    Optional<Product> findById(UUID id);

    @Find
    Optional<Product> findBySlug(String slug);

    @Find
    Optional<Product> findBySku(String sku);

    @Find
    Page<Product> findAll(PageRequest pageRequest);

    @Find
    Page<Product> findByCategoryId(UUID categoryId, PageRequest pageRequest);

    /**
     * Recherche avancée utilisant JSONB de PostgreSQL 18.
     * Exemple: Recherche les produits ayant un attribut spécifique.
     */
    @Query("SELECT p FROM Product p WHERE p.attributes ->> :key = :value")
    Page<Product> findByAttribute(String key, String value, PageRequest pageRequest);

    /**
     * Recherche plein texte avancée sur le nom, description et SKU.
     * Utilise websearch_to_tsquery pour supporter les guillemets, moins, etc.
     * Les résultats sont triés par pertinence (ts_rank).
     */
    @Query("SELECT p FROM Product p WHERE p.searchVector @@ websearch_to_tsquery('french', :query) " +
           "ORDER BY ts_rank(p.searchVector, websearch_to_tsquery('french', :query)) DESC")
    Page<Product> searchFullText(String query, PageRequest pageRequest);

    /**
     * Recherche multi-critères avec filtres.
     * Combine recherche textuelle, catégorie et gamme de prix.
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:query IS NULL OR p.searchVector @@ websearch_to_tsquery('french', :query)) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:available IS NULL OR p.active = :available) " +
           "ORDER BY CASE WHEN :query IS NOT NULL THEN ts_rank(p.searchVector, websearch_to_tsquery('french', :query)) ELSE 0 END DESC")
    Page<Product> searchByCriteria(String query, UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean available, PageRequest pageRequest, Order<Product> order);
}
