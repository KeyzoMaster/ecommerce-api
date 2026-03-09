package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import com.lemzo.ecommerce.core.domain.shipping.ShippingMethod;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import com.lemzo.ecommerce.domain.inventory.service.InventoryService;
import com.lemzo.ecommerce.domain.marketing.service.MarketingService;
import com.lemzo.ecommerce.domain.sales.api.dto.OrderCreateRequest;
import com.lemzo.ecommerce.domain.sales.api.dto.OrderResponse;
import com.lemzo.ecommerce.domain.sales.api.dto.ShippingMethodResponse;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import com.lemzo.ecommerce.payment.service.PaymentGatewayProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service orchestrant le cycle de vie des ventes.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SalesService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PaymentGatewayProvider paymentGatewayProvider;
    private final ShippingRateProvider shippingRateProvider;
    private final MarketingService marketingService;

    /**
     * Crée une commande, réserve le stock et initialise le paiement.
     */
    @Transactional
    @Audit(action = "ORDER_PLACE")
    public OrderResponse placeOrder(UUID userId, OrderCreateRequest request) {
        var order = new Order();
        order.setUserId(userId);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(request.shippingAddress());
        order.setCouponCode(request.couponCode());
        
        var methodStr = Optional.ofNullable(request.shippingMethod()).orElse("STANDARD");
        var shippingMethod = ShippingMethod.valueOf(methodStr.toUpperCase());
        order.setShippingMethod(shippingMethod.name());

        // 1. Transformation fonctionnelle des requêtes en OrderItems avec réservation de stock
        var items = request.items().stream()
                .map(itemReq -> {
                    var product = productRepository.findById(itemReq.productId())
                            .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + itemReq.productId()));
                    
                    inventoryService.reserveStock(product.getId(), itemReq.quantity());
                    
                    return new OrderItem(
                            product.getId(), 
                            product.getStoreId(),
                            itemReq.quantity(), 
                            product.getPrice(),
                            product.getWeight(),
                            product.getShippingConfig()
                    );
                })
                .toList();

        items.forEach(order::addItem);

        // 2. Calcul des montants via Stream
        var itemsTotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Application du coupon
        var discount = marketingService.validateAndApplyCoupon(request.couponCode(), itemsTotal);
        order.setDiscountAmount(discount);

        // 4. Calcul dynamique des frais de port
        var shippingCost = shippingRateProvider.calculateRate(
                null, 
                order.getShippingAddress(),
                shippingMethod,
                itemsTotal.subtract(discount),
                order.getItems()
        );
        order.setShippingCost(shippingCost);

        // 5. Calcul du prix total final
        var finalPrice = itemsTotal.subtract(discount).add(shippingCost);
        order.setTotalPrice(finalPrice);

        // 6. Persistance
        var savedOrder = orderRepository.insert(order);

        // 7. Initialisation du paiement
        var gateway = paymentGatewayProvider.getGateway(request.paymentProvider());
        var paymentResult = gateway.initiate(
                finalPrice, 
                savedOrder.getCurrency(), 
                savedOrder.getId().toString(), 
                "Commande " + savedOrder.getOrderNumber()
        );

        if (!paymentResult.success()) {
            throw new BusinessRuleException("error.sales.payment_initiation_failed", paymentResult.errorMessage());
        }

        return OrderResponse.from(savedOrder, paymentResult.redirectUrl());
    }

    public List<ShippingMethodResponse> getAvailableShippingMethods() {
        return Arrays.stream(ShippingMethod.values())
                .map(m -> new ShippingMethodResponse(m.name(), m.getLabel(), BigDecimal.ZERO, m.getMaxDays()))
                .toList();
    }

    /**
     * Récupère les commandes liées à une boutique.
     */
    public List<OrderResponse> getOrdersByStore(UUID storeId) {
        return orderRepository.findByStoreId(storeId, jakarta.data.page.PageRequest.ofPage(1).size(50))
                .stream()
                .map(o -> OrderResponse.from(o, null))
                .toList();
    }

    /**
     * Met à jour le statut d'une commande.
     */
    @Transactional
    @Audit(action = "ORDER_STATUS_UPDATE")
    public Order updateOrderStatus(UUID orderId, Order.OrderStatus status) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée: " + orderId));
        
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
