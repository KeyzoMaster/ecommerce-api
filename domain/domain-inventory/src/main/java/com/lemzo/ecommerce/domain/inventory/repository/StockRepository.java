package com.lemzo.ecommerce.domain.inventory.repository;

import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository {

    @Insert
    Stock insert(Stock stock);

    @Save
    Stock save(Stock stock);

    @Find
    Optional<Stock> findByProductId(UUID productId);

    @Query("SELECT s FROM Stock s WHERE s.quantity <= s.lowStockThreshold")
    List<Stock> findLowStocks();
}
