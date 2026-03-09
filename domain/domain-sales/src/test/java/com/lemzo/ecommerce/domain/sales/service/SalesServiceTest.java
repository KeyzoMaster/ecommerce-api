package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import com.lemzo.ecommerce.core.contract.payment.PaymentPort.PaymentResult;
import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.core.domain.shipping.ShippingMethod;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import com.lemzo.ecommerce.domain.inventory.service.InventoryService;
import com.lemzo.ecommerce.domain.marketing.service.MarketingService;
import com.lemzo.ecommerce.domain.sales.api.dto.OrderCreateRequest;
import com.lemzo.ecommerce.domain.sales.api.dto.OrderResponse;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import com.lemzo.ecommerce.payment.service.PaymentGatewayProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGatewayProvider paymentGatewayProvider;

    @Mock
    private ShippingRateProvider shippingRateProvider;

    @Mock
    private MarketingService marketingService;

    @Mock
    private PaymentPort paymentGateway;

    @InjectMocks
    private SalesService salesService;

    private UUID userId;
    private UUID productId;
    private Product product;
    private Address shippingAddress;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        product = new Product("Smartphone", "smartphone", "SKU-123", new BigDecimal("50000"), null);
        product.setId(productId);
        product.setWeight(new BigDecimal("0.5"));
        
        shippingAddress = Address.builder()
                .label("Maison")
                .street("Street")
                .city("Dakar")
                .zipCode("10000")
                .country("Sénégal")
                .build();
    }

    @Test
    @DisplayName("Should successfully place an order")
    void shouldPlaceOrderSuccessfully() {
        // Arrange
        var itemRequest = new OrderCreateRequest.OrderItemRequest(productId, 2);
        var request = new OrderCreateRequest(List.of(itemRequest), "stripe", "EXPRESS", null, shippingAddress);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(marketingService.validateAndApplyCoupon(any(), any())).thenReturn(BigDecimal.ZERO);
        when(paymentGatewayProvider.getGateway("stripe")).thenReturn(paymentGateway);
        when(paymentGateway.initiate(any(), any(), any(), any()))
                .thenReturn(PaymentResult.ok("tx_123", "http://pay.me"));
        when(orderRepository.insert(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });
        when(shippingRateProvider.calculateRate(any(), any(), any(), any(), any())).thenReturn(new BigDecimal("5000"));

        // Act
        OrderResponse response = salesService.placeOrder(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals("http://pay.me", response.paymentUrl());
        verify(marketingService).validateAndApplyCoupon(null, new BigDecimal("100000"));
        verify(inventoryService).reserveStock(productId, 2);
        verify(orderRepository).insert(any(Order.class));
        verify(shippingRateProvider).calculateRate(any(), eq(shippingAddress), eq(ShippingMethod.EXPRESS), any(), any());
    }

    @Test
    @DisplayName("Should apply coupon discount correctly")
    void shouldApplyCouponDiscount() {
        var itemRequest = new OrderCreateRequest.OrderItemRequest(productId, 1);
        var request = new OrderCreateRequest(List.of(itemRequest), "stripe", "STANDARD", "PROMO10", shippingAddress);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(marketingService.validateAndApplyCoupon("PROMO10", new BigDecimal("50000"))).thenReturn(new BigDecimal("5000"));
        when(shippingRateProvider.calculateRate(any(), any(), any(), any(), any())).thenReturn(new BigDecimal("2000"));
        when(paymentGatewayProvider.getGateway("stripe")).thenReturn(paymentGateway);
        when(paymentGateway.initiate(any(), any(), any(), any())).thenReturn(PaymentResult.ok("tx", "url"));
        
        when(orderRepository.insert(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        salesService.placeOrder(userId, request);

        verify(paymentGateway).initiate(eq(new BigDecimal("47000")), any(), any(), any()); // (50000 - 5000) + 2000
    }

    @Test
    @DisplayName("Should fail if product not found")
    void shouldFailIfProductNotFound() {
        var itemRequest = new OrderCreateRequest.OrderItemRequest(productId, 1);
        var request = new OrderCreateRequest(List.of(itemRequest), "stripe", "STANDARD", null, shippingAddress);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> salesService.placeOrder(userId, request));
    }

    @Test
    @DisplayName("Should fail if payment initiation fails")
    void shouldFailIfPaymentFails() {
        var itemRequest = new OrderCreateRequest.OrderItemRequest(productId, 1);
        var request = new OrderCreateRequest(List.of(itemRequest), "stripe", "STANDARD", null, shippingAddress);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(paymentGatewayProvider.getGateway("stripe")).thenReturn(paymentGateway);
        when(paymentGateway.initiate(any(), any(), any(), any()))
                .thenReturn(PaymentResult.error("ERR_PAY", "Error"));
        when(orderRepository.insert(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });
        when(shippingRateProvider.calculateRate(any(), any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

        assertThrows(BusinessRuleException.class, () -> salesService.placeOrder(userId, request));
    }
}
