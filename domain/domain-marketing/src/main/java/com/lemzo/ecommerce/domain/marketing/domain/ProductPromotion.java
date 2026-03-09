package com.lemzo.ecommerce.domain.marketing.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Entité représentant une promotion sur un produit spécifique.
 * Protégée par des Temporal Constraints (WITHOUT OVERLAPS) au niveau de PostgreSQL 18.
 */
@Entity
@Table(name = "marketing_product_promotions")
@Getter
@Setter
@NoArgsConstructor
public class ProductPromotion extends AbstractEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    public ProductPromotion(UUID productId, BigDecimal discountValue, LocalDateTime startDate, LocalDateTime endDate) {
        this.productId = productId;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Vérifie dynamiquement si la promotion est active.
     */
    public boolean isActive() {
        var now = LocalDateTime.now();
        return Optional.ofNullable(startDate).map(now::isAfter).orElse(true) &&
               Optional.ofNullable(endDate).map(now::isBefore).orElse(true);
    }
}
