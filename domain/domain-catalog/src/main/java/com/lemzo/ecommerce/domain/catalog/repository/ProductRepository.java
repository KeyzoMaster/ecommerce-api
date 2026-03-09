package com.lemzo.ecommerce.domain.catalog.repository;

import com.lemzo.ecommerce.domain.catalog.domain.Product;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import jakarta.data.repository.Param;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les produits utilisant Jakarta Data 1.0.
 * Utilise uniquement des méthodes @Find pour assurer la compatibilité avec la pagination.
 */
@Repository
public interface ProductRepository {

    @Insert
    Product insert(Product product);

    @Update
    Product update(Product product);

    @Find
    Optional<Product> findById(UUID id);

    @Find
    Optional<Product> findBySlug(String slug);

    @Find
    Page<Product> findAll(PageRequest pageRequest);

    @Find
    Page<Product> findByCategoryId(UUID categoryId, PageRequest pageRequest);

    @Find
    Page<Product> findByNameLike(@Param("name") String name, PageRequest pageRequest);

    @Delete
    void delete(Product product);
}
