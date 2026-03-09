package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.contract.payment.PaymentResult;
import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.domain.catalog.service.CatalogService;
import com.lemzo.ecommerce.domain.inventory.service.InventoryService;
import com.lemzo.ecommerce.domain.marketing.service.MarketingService;
import com.lemzo.ecommerce.domain.sales.api.dto.OrderCreateRequest;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import com.lemzo.ecommerce.iam.service.UserService;
import com.lemzo.ecommerce.payment.service.PaymentGatewayProvider;
import com.lemzo.ecommerce.core.annotation.Audit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service de traitement des ventes et commandes.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SalesService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CatalogService catalogService;
    private final InventoryService inventoryService;
    private final MarketingService marketingService;
    private final UserService userService;
    private final PaymentGatewayProvider paymentGatewayProvider;
    private final ShippingRateProvider shippingRateProvider;

    @Transactional
    @Audit(action = "ORDER_CREATE")
    public Order placeOrder(final UUID userId, final OrderCreateRequest request) {
        final var cart = cartService.getCart(userId)
                .orElseThrow(() -> new BusinessRuleException("error.sales.cart_empty"));

        if (cart.items().isEmpty()) {
            throw new BusinessRuleException("error.sales.cart_empty");
        }

        final var user = userService.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.sales.user_not_found"));
        
        final var shippingAddress = user.getAddresses().stream()
                .filter(addr -> addr.getTechnicalId().equals(request.shippingAddressId()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("error.sales.address_not_found"));

        final var order = new Order(userId, generateOrderNumber());
        order.setShippingAddress(shippingAddress);
        order.setShippingMethod(request.shippingMethod().name());

        // 1. Vérifier stocks et créer les lignes
        cart.items().forEach(item -> {
            if (!inventoryService.isAvailable(item.productId(), item.quantity())) {
                throw new BusinessRuleException("error.inventory.insufficient_stock", item.productName());
            }
            final var product = catalogService.findById(item.productId()).orElseThrow();
            final var orderItem = new OrderItem(
                    item.productId(),
                    product.getCategory().getId(),
                    item.quantity(),
                    item.unitPrice(),
                    product.getWeight(),
                    product.getShippingConfig()
            );
            order.addItem(orderItem);
        });

        // 2. Calculs financiers
        final var itemsTotal = cart.getTotalPrice();
        final var discount = marketingService.applyCoupon(request.couponCode(), itemsTotal)
                .orElse(BigDecimal.ZERO);
        
        final var shippingCost = shippingRateProvider.calculateRate(
                shippingAddress, request.shippingMethod(), itemsTotal, order.getItems());

        order.setShippingCost(shippingCost);
        order.setDiscountAmount(discount);
        order.setCouponCode(request.couponCode());
        order.setTotalAmount(itemsTotal.add(shippingCost).subtract(discount));

        // 3. Persistance
        final var savedOrder = orderRepository.save(order);

        // 4. Décrémentation stocks
        cart.items().forEach(item -> inventoryService.updateStock(item.productId(), -item.quantity()));
        
        // 5. Nettoyage panier
        cartService.clearCart(userId);

        return savedOrder;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    public PaymentResult initiatePayment(final Order order, final String provider) {
        final var gateway = paymentGatewayProvider.getGateway(provider);
        return gateway.initiate(
                order.getTotalAmount(),
                order.getCurrency(),
                order.getOrderNumber(),
                "Commande #" + order.getOrderNumber()
        );
    }

    @Transactional
    @Audit(action = "ORDER_STATUS_UPDATE")
    public Order updateStatus(final UUID orderId, final Order.OrderStatus status) {
        final var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée"));
        
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
