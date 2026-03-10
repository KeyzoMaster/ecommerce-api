package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import com.lemzo.ecommerce.domain.core.catalog.CatalogPort;
import com.lemzo.ecommerce.domain.core.iam.UserPort;
import com.lemzo.ecommerce.domain.core.inventory.InventoryPort;
import com.lemzo.ecommerce.domain.core.marketing.MarketingPort;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Service orchestrant le tunnel de vente.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SalesService {

    private final OrderRepository orderRepository;
    private final CatalogPort catalogPort;
    private final MarketingPort marketingPort;
    private final Instance<PaymentPort> paymentPorts;
    private final UserPort userPort;
    private final CartService cartService;

    // Injection du port d'inventaire
    private final InventoryPort inventoryPort;

    public Page<Order> listOrdersByUserId(final UUID userId, final PageRequest pageRequest) {
        return orderRepository.findByUserId(userId, pageRequest);
    }

    public Optional<Order> findOrderByNumber(final String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @HasPermission(resource = ResourceType.SALES, action = PbacAction.READ, checkOwnership = true)
    public Page<Order> listOrdersByStoreId(final UUID storeId, final PageRequest pageRequest) {
        return orderRepository.findByStoreId(storeId, pageRequest);
    }

    @Transactional
    @Audit(action = "ORDER_PLACE")
    public Order placeOrder(final UUID userId, final String shippingAddressId,
                            final String couponCode, final String paymentProvider) {

        final var cart = cartService.getCart(userId)
                .orElseThrow(() -> new BusinessRuleException("Panier vide"));

        if (cart.items().isEmpty()) {
            throw new BusinessRuleException("Le panier ne contient aucun article");
        }

        final Order order = new Order(userId, "ORD-" + System.currentTimeMillis());
        BigDecimal subtotal = BigDecimal.ZERO;

        for (final var cartItem : cart.items()) {
            final var product = (com.lemzo.ecommerce.domain.catalog.domain.Product) catalogPort.findProductById(cartItem.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

            // 1. Vérification sécurisée du stock avant tout calcul
            if (!inventoryPort.isAvailable(product.getId(), cartItem.quantity())) {
                throw new BusinessRuleException("Stock insuffisant pour le produit : " + product.getName());
            }

            final OrderItem item = new OrderItem(
                    product.getId(),
                    product.getStoreId(),
                    cartItem.quantity(),
                    product.getPrice(),
                    product.getWeight(),
                    product.getShippingConfig()
            );
            order.addItem(item);

            final BigDecimal itemSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            // 2. Décrémentation immédiate du stock via une valeur négative
            inventoryPort.updateStock(product.getId(), -cartItem.quantity());
        }

        // Appliquer Coupon si présent
        if (couponCode != null && !couponCode.isBlank()) {
            final BigDecimal discount = marketingPort.applyCoupon(couponCode, subtotal)
                    .orElse(BigDecimal.ZERO);
            order.setDiscountAmount(discount);
            order.setCouponCode(couponCode);
        }

        order.setTotalAmount(subtotal.subtract(order.getDiscountAmount()).add(order.getShippingCost()));

        final Order savedOrder = orderRepository.insert(order);
        cartService.clearCart(userId);

        return savedOrder;
    }

    @Transactional
    @Audit(action = "ORDER_STATUS_UPDATE")
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.MANAGE)
    public Order updateStatus(final UUID orderId, final Order.OrderStatus status) {
        final Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée"));

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Impossible de modifier une commande annulée");
        }

        order.setStatus(status);
        return orderRepository.update(order);
    }

    public Optional<Order> findById(final UUID id) {
        return orderRepository.findById(id);
    }
}