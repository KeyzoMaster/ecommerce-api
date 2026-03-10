package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.iam.domain.Store;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les boutiques.
 */
@Repository
public interface StoreRepository {

    @Insert
    Store insert(Store store);

    @Update
    Store update(Store store);

    @Find
    Optional<Store> findById(UUID id);

    @Find
    Optional<Store> findByName(String name);

    @Find
    Optional<Store> findBySlug(String slug);

    @Delete
    void delete(Store store);
}
