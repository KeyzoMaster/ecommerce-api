package com.lemzo.ecommerce.domain.marketing.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Entité représentant un coupon de réduction.
 */
@Entity
@Table(name = "marketing_coupons")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String type; // PERCENTAGE, FIXED_AMOUNT

    @Column(nullable = false)
    private BigDecimal value;

    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "max_usages")
    private Integer maxUsages;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;

    public Coupon(final String code, final String type, final BigDecimal value) {
        super();
        this.code = code;
        this.type = type;
        this.value = value;
    }

    public boolean isValid() {
        final var now = OffsetDateTime.now();
        final boolean dateStarted = Optional.ofNullable(startDate).map(now::isAfter).orElse(true);
        final boolean notExpired = Optional.ofNullable(endDate).map(now::isBefore).orElse(true);
        final boolean usageAvailable = Optional.ofNullable(maxUsages).map(max -> usageCount < max).orElse(true);

        return dateStarted && notExpired && usageAvailable;
    }
}
