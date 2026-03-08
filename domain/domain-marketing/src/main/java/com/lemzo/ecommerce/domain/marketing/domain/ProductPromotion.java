package com.lemzo.ecommerce.domain.marketing.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entité représentant une promotion sur un produit spécifique.
 * Utilise les contraintes temporelles WITHOUT OVERLAPS de PostgreSQL 18.
 */
@Entity
@Table(name = "marketing_product_promotions")
@Getter
@Setter
@NoArgsConstructor
public class ProductPromotion extends AbstractEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private BigDecimal discountValue;

    /**
     * Période de validité utilisant le type 'tstzrange' de PostgreSQL.
     * La base de données applique une contrainte d'exclusion : 
     * EXCLUDE USING gist (product_id WITH =, validity_period WITHOUT OVERLAPS)
     */
    @Column(name = "validity_period", columnDefinition = "tstzrange", nullable = false)
    private String validityPeriod;

    /**
     * Colonne virtuelle calculant si la promotion est actuellement active.
     */
    @Column(name = "is_active_now", insertable = false, updatable = false)
    private boolean activeNow;

    public ProductPromotion(UUID productId, BigDecimal discountValue, String validityPeriod) {
        this.productId = productId;
        this.discountValue = discountValue;
        this.validityPeriod = validityPeriod;
    }
}
