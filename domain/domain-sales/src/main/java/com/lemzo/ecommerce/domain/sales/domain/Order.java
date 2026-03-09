package com.lemzo.ecommerce.domain.sales.domain;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une commande.
 */
@Entity
@Table(name = "sales_orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends AbstractEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Montant total de la commande (Articles + Frais de port - Remises).
     */
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "XOF";

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "coupon_code")
    private String couponCode;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "shipping_street")),
        @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zip_code")),
        @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))
    })
    private Address shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    public Order(final UUID userId, final String orderNumber) {
        super();
        this.userId = userId;
        this.orderNumber = orderNumber;
    }

    public void addItem(final OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public enum OrderStatus {
        PENDING, PAID, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }
}
