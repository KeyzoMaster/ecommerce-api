package com.lemzo.ecommerce.domain.inventory.repository;

import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import jakarta.data.repository.By; // Ajout de l'import
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository {

    @Insert
    Stock insert(Stock stock);

    @Update
    Stock update(Stock stock);

    @Find
        // Ajout de @By pour lier le paramètre à la requête SQL
    Optional<Stock> findByProductId(@By("productId") UUID productId);

    @Query("SELECT s FROM Stock s WHERE s.quantity <= s.lowStockThreshold")
    List<Stock> findLowStocks();

    @Find
    Page<Stock> findAll(PageRequest pageRequest);

    @Delete
    void delete(Stock stock);
}