package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.iam.domain.Store;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les boutiques utilisant Jakarta Data 1.0.
 */
@Repository
public interface StoreRepository extends BasicRepository<Store, UUID> {

    @Find
    Optional<Store> findBySlug(String slug);

    @Find
    Page<Store> findAll(PageRequest pageRequest);
}
