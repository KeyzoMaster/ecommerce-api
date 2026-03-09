package com.lemzo.ecommerce.domain.sales.repository;

import com.lemzo.ecommerce.domain.sales.domain.Order;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les commandes.
 */
@Repository
public interface OrderRepository {

    @Insert
    Order insert(Order order);

    @Update
    Order update(Order order);

    @Find
    Optional<Order> findById(UUID id);

    @Find
    Optional<Order> findByOrderNumber(String orderNumber);

    @Find
    Page<Order> findAll(PageRequest pageRequest);

    @Find
    Page<Order> findByUserId(UUID userId, PageRequest pageRequest);

    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.storeId = :storeId")
    Page<Order> findByStoreId(UUID storeId, PageRequest pageRequest);

    @Query("SELECT COUNT(o) FROM Order o")
    long count();

    @Delete
    void delete(Order order);
}
