package com.lemzo.ecommerce.domain.inventory.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import java.util.UUID;

/**
 * Entité représentant le stock d'un produit.
 */
@Entity
@Table(name = "inventory_stocks")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends AbstractEntity {

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(nullable = false)
    private int quantity = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold = 5;

    public Stock(final UUID productId, final int quantity) {
        super();
        this.productId = productId;
        this.quantity = quantity;
    }
}
