package com.lemzo.ecommerce.domain.inventory.repository;

import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les stocks utilisant Jakarta Data 1.0.
 */
@Repository
public interface StockRepository extends BasicRepository<Stock, UUID> {

    @Find
    Optional<Stock> findByProductId(UUID productId);

    @Query("from Stock s WHERE s.quantity <= s.lowStockThreshold")
    List<Stock> findLowStocks();

    @Find
    Page<Stock> findAll(PageRequest pageRequest);
}
