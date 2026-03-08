package com.lemzo.ecommerce.domain.marketing.repository;

import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository {

    @Insert
    Coupon insert(Coupon coupon);

    @Save
    Coupon save(Coupon coupon);

    @Find
    Optional<Coupon> findByCode(String code);

    @Find
    Optional<Coupon> findById(UUID id);
}
