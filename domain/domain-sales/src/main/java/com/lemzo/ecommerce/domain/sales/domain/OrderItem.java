package com.lemzo.ecommerce.domain.sales.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import com.lemzo.ecommerce.core.entity.converter.JsonbConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Entité représentant une ligne de commande.
 */
@Entity
@Table(name = "sales_order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "weight")
    private BigDecimal weight;

    @Convert(converter = JsonbConverter.class)
    @Column(name = "shipping_config", columnDefinition = "jsonb")
    private Map<String, Object> shippingConfig;

    /**
     * Sous-total calculé par la base de données (quantity * unit_price).
     * Virtual Generated Column PostgreSQL 18.
     */
    @Column(name = "subtotal", insertable = false, updatable = false, 
            columnDefinition = "numeric GENERATED ALWAYS AS (quantity * unit_price) VIRTUAL")
    private BigDecimal subtotal;

    public OrderItem(UUID productId, UUID storeId, int quantity, BigDecimal unitPrice, BigDecimal weight, Map<String, Object> shippingConfig) {
        this.productId = productId;
        this.storeId = storeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.weight = weight;
        this.shippingConfig = shippingConfig;
    }
}
