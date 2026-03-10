package com.lemzo.ecommerce.domain.marketing.repository;

import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import jakarta.data.repository.By; // <-- AJOUT DE L'IMPORT
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les coupons utilisant Jakarta Data 1.0.
 */
@Repository
public interface CouponRepository {

    @Insert
    Coupon insert(Coupon coupon);

    @Update
    Coupon update(Coupon coupon);

    @Find
    Optional<Coupon> findById(@By("id") UUID id); // <-- AJOUT DE @By

    @Find
    Optional<Coupon> findByCode(@By("code") String code); // <-- AJOUT DE @By

    @Find
    Page<Coupon> findAll(PageRequest pageRequest);

    @Delete
    void delete(Coupon coupon);
}