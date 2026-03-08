package com.lemzo.ecommerce.domain.sales.repository;

import com.lemzo.ecommerce.domain.sales.domain.Order;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository {

    @Insert
    Order insert(Order order);

    @Save
    Order save(Order order);

    @Find
    Optional<Order> findById(UUID id);

    @Find
    Optional<Order> findByOrderNumber(String orderNumber);

    @Find
    Page<Order> findByUserId(UUID userId, PageRequest pageRequest);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.storeId = ?1 ORDER BY o.createdAt DESC")
    Page<Order> findByStoreId(UUID storeId, PageRequest pageRequest);
}
