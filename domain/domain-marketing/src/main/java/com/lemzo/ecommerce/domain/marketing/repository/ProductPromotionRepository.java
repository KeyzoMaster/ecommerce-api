package com.lemzo.ecommerce.domain.marketing.repository;

import com.lemzo.ecommerce.domain.marketing.domain.ProductPromotion;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductPromotionRepository {

    @Insert
    ProductPromotion insert(ProductPromotion promotion);

    @Find
    Optional<ProductPromotion> findById(UUID id);

    /**
     * Récupère la promotion active pour un produit en utilisant l'opérateur de range PostgreSQL (@>).
     */
    @Query("SELECT p FROM ProductPromotion p WHERE p.productId = :productId AND p.validityPeriod @> CURRENT_TIMESTAMP")
    Optional<ProductPromotion> findActivePromotion(UUID productId);
}
