package com.lemzo.ecommerce.domain.marketing.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "marketing_coupons")
@Getter
@Setter
@NoArgsConstructor
public class Coupon extends AbstractEntity {

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "is_active")
    private boolean active = true;

    public Coupon(String code, DiscountType type, BigDecimal value) {
        this.code = code;
        this.type = type;
        this.value = value;
    }

    public boolean isValid() {
        var now = LocalDateTime.now();
        
        var dateStarted = Optional.ofNullable(startDate)
                .map(start -> now.isAfter(start))
                .orElse(true);
        
        var notExpired = Optional.ofNullable(endDate)
                .map(end -> now.isBefore(end))
                .orElse(true);

        var usageAvailable = Optional.ofNullable(usageLimit)
                .map(limit -> usedCount < limit)
                .orElse(true);
        
        return active && dateStarted && notExpired && usageAvailable;
    }

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }
}
