package com.lemzo.ecommerce.domain.sales.repository;

import com.lemzo.ecommerce.domain.sales.domain.Order;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les commandes utilisant Jakarta Data 1.0.
 */
@Repository
public interface OrderRepository extends BasicRepository<Order, UUID> {

    @Find
    Optional<Order> findByOrderNumber(String orderNumber);

    @Find
    Page<Order> findByUserId(UUID userId, PageRequest pageRequest);

    @Find
    Page<Order> findAll(PageRequest pageRequest);

    @Query("from Order o JOIN o.items i WHERE i.storeId = ?1 ORDER BY o.createdAt DESC")
    Page<Order> findByStoreId(UUID storeId, PageRequest pageRequest);
}
