package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.iam.domain.Store;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository {

    @Insert
    Store insert(Store store);

    @Save
    Store save(Store store);

    @Find
    Optional<Store> findById(UUID id);

    @Find
    Optional<Store> findBySlug(String slug);
}
