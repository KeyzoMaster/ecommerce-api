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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service orchestrant le cycle de vie des ventes.
 */
@ApplicationScoped
public class SalesService {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private InventoryService inventoryService;

    @Inject
    private PaymentGatewayProvider paymentGatewayProvider;

    @Inject
    private ShippingRateProvider shippingRateProvider;

    @Inject
    private MarketingService marketingService;

    /**
     * Crée une commande, réserve le stock et initialise le paiement.
     */
    @Transactional
    @Audit(action = "ORDER_PLACE")
    public OrderResponse placeOrder(UUID userId, OrderCreateRequest request) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(request.shippingAddress());
        order.setCouponCode(request.couponCode());
        
        String methodStr = Optional.ofNullable(request.shippingMethod()).orElse("STANDARD");
        ShippingMethod shippingMethod = ShippingMethod.valueOf(methodStr.toUpperCase());
        order.setShippingMethod(shippingMethod.name());

        final BigDecimal[] itemsTotal = {BigDecimal.ZERO};

        // Traitement fonctionnel des articles
        request.items().forEach(itemReq -> {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + itemReq.productId()));

            // 1. Réserver le stock
            inventoryService.reserveStock(product.getId(), itemReq.quantity());

            // 2. Créer la ligne de commande (avec snapshot poids/config)
            OrderItem item = new OrderItem(
                    product.getId(), 
                    product.getStoreId(),
                    itemReq.quantity(), 
                    product.getPrice(),
                    product.getWeight(),
                    product.getShippingConfig()
            );
            order.addItem(item);
            
            BigDecimal itemSubtotal = product.getPrice().multiply(new BigDecimal(itemReq.quantity()));
            itemsTotal[0] = itemsTotal[0].add(itemSubtotal);
        });

        // 3. Application du coupon de réduction (sur le total des articles)
        BigDecimal discount = marketingService.validateAndApplyCoupon(request.couponCode(), itemsTotal[0]);
        order.setDiscountAmount(discount);

        // 4. Calcul dynamique des frais de port
        BigDecimal shippingCost = shippingRateProvider.calculateRate(
                null, 
                order.getShippingAddress(),
                shippingMethod,
                itemsTotal[0].subtract(discount), // Le montant pour la gratuité peut dépendre du total après remise ? 
                // Généralement c'est avant remise pour encourager l'achat mais dépend de la politique.
                // Restons sur itemsTotal[0] pour la gratuité.
                order.getItems()
        );
        order.setShippingCost(shippingCost);

        // 5. Calcul du prix total final
        BigDecimal finalPrice = itemsTotal[0].subtract(discount).add(shippingCost);
        order.setTotalPrice(finalPrice);

        // 6. Sauvegarder la commande
        Order savedOrder = orderRepository.insert(order);

        // 7. Initialiser le paiement
        PaymentPort gateway = paymentGatewayProvider.getGateway(request.paymentProvider());
        PaymentPort.PaymentResult paymentResult = gateway.initiate(
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
        // Pour simplifier sans pagination ici, ou utiliser un PageRequest par défaut
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée: " + orderId));
        
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
