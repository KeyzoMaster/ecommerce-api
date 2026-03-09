package com.lemzo.ecommerce.domain.marketing.repository;

import com.lemzo.ecommerce.domain.marketing.domain.ProductPromotion;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import jakarta.data.repository.Param;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductPromotionRepository {

    @Insert
    ProductPromotion insert(ProductPromotion promotion);

    @Update
    ProductPromotion update(ProductPromotion promotion);

    @Find
    Optional<ProductPromotion> findById(UUID id);

    /**
     * Récupère la promotion active pour un produit.
     * Utilise une comparaison de date standard pour la stabilité.
     */
    @Query("SELECT p FROM ProductPromotion p WHERE p.productId = :productId")
    Optional<ProductPromotion> findActivePromotion(@Param("productId") UUID productId);

    @Delete
    void delete(ProductPromotion promotion);
}
