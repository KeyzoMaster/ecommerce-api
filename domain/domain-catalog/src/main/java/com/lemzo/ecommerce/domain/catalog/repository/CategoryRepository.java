package com.lemzo.ecommerce.domain.catalog.repository;

import com.lemzo.ecommerce.domain.catalog.domain.Category;
import jakarta.data.repository.By;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les catégories.
 */
@Repository
public interface CategoryRepository {

    @Insert
    Category insert(Category category);

    @Update
    Category update(Category category);

    @Find
    Optional<Category> findById(@By("id") UUID id);

    @Find
    Optional<Category> findBySlug(@By("slug") String slug);

    @Find
    List<Category> findAll();

    @Find
    List<Category> findByParentId(@By("parentId") UUID parentId);

    @Delete
    void delete(Category category);
}