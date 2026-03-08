package com.lemzo.ecommerce.domain.catalog.repository;

import com.lemzo.ecommerce.domain.catalog.domain.Category;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository {

    @Insert
    Category insert(Category category);

    @Save
    Category save(Category category);

    @Find
    Optional<Category> findById(UUID id);

    @Find
    Optional<Category> findBySlug(String slug);

    @Find
    List<Category> findAll();

    @Find
    List<Category> findByParentIsNull();
}
