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

/**
 * Entité représentant un coupon de réduction.
 */
@Entity
@Table(name = "marketing_coupons")
@Getter
@Setter
@NoArgsConstructor
public class Coupon extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private int usedCount = 0;

    /**
     * Colonne virtuelle PostgreSQL 18.
     * Déterminée automatiquement par la DB : (end_date < now()).
     */
    @Column(name = "is_expired", insertable = false, updatable = false,
            columnDefinition = "boolean GENERATED ALWAYS AS (end_date < CURRENT_TIMESTAMP) VIRTUAL")
    private boolean expired;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Coupon(String code, DiscountType type, BigDecimal value) {
        this.code = code;
        this.type = type;
        this.value = value;
    }

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        
        boolean dateStarted = Optional.ofNullable(startDate).map(now::isAfter).orElse(true);
                           
        boolean usageAvailable = Optional.ofNullable(usageLimit)
                .map(limit -> usedCount < limit)
                .orElse(true);
        
        // Utilisation de la colonne virtuelle 'expired' (gérée par la DB)
        return active && dateStarted && !expired && usageAvailable;
    }

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }
}
