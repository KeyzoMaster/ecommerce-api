package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.domain.core.catalog.CatalogPort;
import com.lemzo.ecommerce.domain.core.marketing.MarketingPort;
import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import com.lemzo.ecommerce.domain.core.iam.UserPort;
import com.lemzo.ecommerce.domain.core.inventory.InventoryPort;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.sales.domain.Cart;
import com.lemzo.ecommerce.domain.sales.domain.CartItem;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.inject.Instance;
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
@DisplayName("SalesService Unit Tests")
class SalesServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private CatalogPort catalogPort;

    @Mock
    private MarketingPort marketingPort;

    @Mock
    private InventoryPort inventoryPort;

    @Mock
    private UserPort userPort;

    @Mock
    private Instance<PaymentPort> paymentPorts;

    @InjectMocks
    private SalesService salesService;

    private UUID userId;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        
        final Category category = new Category("Electronics", "electronics", "Desc");
        final var catIdField = com.lemzo.ecommerce.core.entity.AbstractEntity.class.getDeclaredField("id");
        catIdField.setAccessible(true);
        catIdField.set(category, UUID.randomUUID());

        product = new Product("Smartphone", "smartphone", "SKU-123", new BigDecimal("50000"), category);
        
        final var prodIdField = com.lemzo.ecommerce.core.entity.AbstractEntity.class.getDeclaredField("id");
        prodIdField.setAccessible(true);
        prodIdField.set(product, UUID.randomUUID());
    }

    @Test
    @DisplayName("Should successfully place an order")
    void shouldPlaceOrderSuccessfully() {
        final CartItem cartItem = new CartItem(product.getId(), "Smartphone", 2, new BigDecimal("50000"));
        final Cart cart = new Cart(userId, List.of(cartItem));
        
        when(cartService.getCart(userId)).thenReturn(Optional.of(cart));
        doReturn(Optional.of(product)).when(catalogPort).findProductById(product.getId());
        when(inventoryPort.isAvailable(product.getId(), 2)).thenReturn(true);
        when(orderRepository.insert(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(marketingPort.applyCoupon(anyString(), any())).thenReturn(Optional.of(new BigDecimal("10000")));

        final Order result = salesService.placeOrder(userId, "addr-123", "COUPON", "paytech");

        assertNotNull(result);
        assertEquals(new BigDecimal("100000"), result.getTotalAmount().add(result.getDiscountAmount())); // subtotal + shipping - discount
        assertEquals(new BigDecimal("10000"), result.getDiscountAmount());
        assertEquals("COUPON", result.getCouponCode());
        assertEquals(1, result.getItems().size());
        
        verify(inventoryPort).decreaseStock(product.getId(), 2);
        verify(orderRepository).insert(any(Order.class));
        verify(cartService).clearCart(userId);
    }

    @Test
    @DisplayName("Should throw exception when placing order with empty cart")
    void shouldThrowWhenCartEmpty() {
        when(cartService.getCart(userId)).thenReturn(Optional.of(new Cart(userId, List.of())));

        assertThrows(BusinessRuleException.class, () -> salesService.placeOrder(userId, "addr", null, null));
    }

    @Test
    @DisplayName("Should throw exception when stock is insufficient")
    void shouldThrowWhenStockInsufficient() {
        final CartItem cartItem = new CartItem(product.getId(), "Smartphone", 2, new BigDecimal("50000"));
        when(cartService.getCart(userId)).thenReturn(Optional.of(new Cart(userId, List.of(cartItem))));
        doReturn(Optional.of(product)).when(catalogPort).findProductById(product.getId());
        when(inventoryPort.isAvailable(product.getId(), 2)).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> salesService.placeOrder(userId, "addr", null, null));
        verify(inventoryPort, never()).decreaseStock(any(), anyInt());
    }

    @Test
    @DisplayName("Should successfully update order status")
    void shouldUpdateOrderStatus() {
        final UUID orderId = UUID.randomUUID();
        final Order order = new Order(userId, "ORD-123");
        order.setStatus(Order.OrderStatus.PENDING);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.update(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        final Order result = salesService.updateStatus(orderId, Order.OrderStatus.SHIPPED);

        assertEquals(Order.OrderStatus.SHIPPED, result.getStatus());
        verify(orderRepository).update(order);
    }

    @Test
    @DisplayName("Should throw exception when updating a cancelled order")
    void shouldThrowWhenUpdatingCancelledOrder() {
        final UUID orderId = UUID.randomUUID();
        final Order order = new Order(userId, "ORD-123");
        order.setStatus(Order.OrderStatus.CANCELLED);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(BusinessRuleException.class, () -> salesService.updateStatus(orderId, Order.OrderStatus.SHIPPED));
        verify(orderRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should fetch paginated orders by user ID")
    void shouldListOrdersByUserId() {
        final PageRequest request = PageRequest.ofPage(1, 10, true);
        @SuppressWarnings("unchecked")
        final Page<Order> mockPage = mock(Page.class);
        
        when(orderRepository.findByUserId(userId, request)).thenReturn(mockPage);

        final Page<Order> result = salesService.listOrdersByUserId(userId, request);

        assertEquals(mockPage, result);
        verify(orderRepository).findByUserId(userId, request);
    }

    @Test
    @DisplayName("Should fetch paginated orders by store ID")
    void shouldListOrdersByStoreId() {
        final UUID storeId = UUID.randomUUID();
        final PageRequest request = PageRequest.ofPage(1, 10, true);
        @SuppressWarnings("unchecked")
        final Page<Order> mockPage = mock(Page.class);
        
        when(orderRepository.findByStoreId(storeId, request)).thenReturn(mockPage);

        final Page<Order> result = salesService.listOrdersByStoreId(storeId, request);

        assertEquals(mockPage, result);
        verify(orderRepository).findByStoreId(storeId, request);
    }
}