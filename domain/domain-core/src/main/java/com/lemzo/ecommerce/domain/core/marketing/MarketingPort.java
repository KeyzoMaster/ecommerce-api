package com.lemzo.ecommerce.domain.core.marketing;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Port pour les opérations marketing.
 */
public interface MarketingPort {
    Optional<BigDecimal> applyCoupon(String code, BigDecimal currentAmount);
    void incrementUsage(String code);
}
