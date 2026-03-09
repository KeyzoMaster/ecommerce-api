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
import java.util.UUID;

/**
 * Entité représentant une promotion sur un produit spécifique.
 */
@Entity
@Table(name = "marketing_product_promotions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPromotion extends AbstractEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;

    public ProductPromotion(final UUID productId, final BigDecimal discountValue, 
                            final OffsetDateTime startDate, final OffsetDateTime endDate) {
        super();
        this.productId = productId;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isActive() {
        final var now = OffsetDateTime.now();
        return Optional.ofNullable(startDate).map(now::isAfter).orElse(true) &&
               Optional.ofNullable(endDate).map(now::isBefore).orElse(true);
    }
}
