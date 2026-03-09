package com.lemzo.ecommerce.domain.sales.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import com.lemzo.ecommerce.core.entity.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
     * Sous-total calculé par la base de données.
     */
    @Column(name = "subtotal", insertable = false, updatable = false, 
            columnDefinition = "numeric GENERATED ALWAYS AS (quantity * unit_price) STORED")
    private BigDecimal subtotal;

    public OrderItem(final UUID productId, final UUID storeId, final int quantity, 
                     final BigDecimal unitPrice, final BigDecimal weight, 
                     final Map<String, Object> shippingConfig) {
        super();
        this.productId = productId;
        this.storeId = storeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.weight = weight;
        this.shippingConfig = shippingConfig;
    }
}
