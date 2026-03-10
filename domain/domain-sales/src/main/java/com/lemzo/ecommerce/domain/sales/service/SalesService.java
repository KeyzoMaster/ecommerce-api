package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.domain.core.catalog.CatalogPort;
import com.lemzo.ecommerce.domain.core.marketing.MarketingPort;
import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import com.lemzo.ecommerce.domain.core.iam.UserPort;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import com.lemzo.ecommerce.domain.sales.domain.SalesFactory;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.core.annotation.Audit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
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
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SalesService {

    private final OrderRepository orderRepository;
    private final CatalogPort catalogPort;
    private final MarketingPort marketingPort;
    private final Instance<PaymentPort> paymentPorts; // Utilisation de Instance pour gérer les multiples implémentations
    private final UserPort userPort;
    private final CartService cartService;

    @Transactional
    @Audit(action = "ORDER_PLACE")
    public Order placeOrder(final UUID userId, final String shippingAddressId, 
                            final String couponCode, final String paymentProvider) {
        
        final var cart = cartService.getCart(userId)
                .orElseThrow(() -> new BusinessRuleException("Panier vide"));

        final String orderNumber = "ORD-" + System.currentTimeMillis();
        final Order order = SalesFactory.createOrder(userId, orderNumber);
        
        final Address address = userPort.findAddressById(userId, shippingAddressId)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée"));
        order.setShippingAddress(address);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (final var cartItem : cart.items()) {
            final var product = (Product) catalogPort.findProductById(cartItem.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé : " + cartItem.productId()));
            
            final OrderItem item = SalesFactory.createItem(
                    product.getId(), 
                    product.getStoreId(), 
                    cartItem.quantity(), 
                    product.getPrice()
            );
            order.addItem(item);
            
            final BigDecimal itemSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
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

        // Sélectionner dynamiquement le port de paiement
        final PaymentPort port = paymentPorts.select(NamedLiteral.of(paymentProvider.toLowerCase())).get();
        port.initiate(
                savedOrder.getTotalAmount(), 
                "XOF", 
                savedOrder.getId().toString(), 
                "Commande " + savedOrder.getOrderNumber()
        );

        // Vider le panier
        cartService.clearCart(userId);

        return savedOrder;
    }

    @Transactional
    @Audit(action = "ORDER_STATUS_UPDATE")
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
