package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.domain.core.catalog.CatalogPort;
import com.lemzo.ecommerce.domain.core.marketing.MarketingPort;
import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import com.lemzo.ecommerce.domain.core.iam.UserPort;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.sales.domain.Cart;
import com.lemzo.ecommerce.domain.sales.domain.CartItem;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
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

/**
 * Tests unitaires pour SalesService.
 */
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
    private UserPort userPort;

    @Mock
    private PaymentPort paymentPort;

    @InjectMocks
    private SalesService salesService;

    private UUID userId;
    private Address shippingAddress;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        
        final var category = new Category("Electronics", "electronics", "Desc");
        final var catIdField = com.lemzo.ecommerce.core.entity.AbstractEntity.class.getDeclaredField("entityId");
        catIdField.setAccessible(true);
        catIdField.set(category, UUID.randomUUID());

        product = new Product("Smartphone", "smartphone", "SKU-123", new BigDecimal("50000"), category);
        
        final var prodIdField = com.lemzo.ecommerce.core.entity.AbstractEntity.class.getDeclaredField("entityId");
        prodIdField.setAccessible(true);
        prodIdField.set(product, UUID.randomUUID());

        shippingAddress = Address.builder()
                .technicalId("addr-123")
                .label("Maison")
                .country("Sénégal")
                .build();
    }

    @Test
    @DisplayName("Should successfully place an order")
    void shouldPlaceOrderSuccessfully() {
        // Arrange
        final var cartItem = new CartItem(product.getId(), "Smartphone", 2, new BigDecimal("50000"));
        final var cart = new Cart(userId, List.of(cartItem));
        
        when(cartService.getCart(userId)).thenReturn(Optional.of(cart));
        when(userPort.findAddressById(userId, "addr-123")).thenReturn(Optional.of(shippingAddress));
        
        doReturn(Optional.of(product)).when(catalogPort).findProductById(any());
        
        when(orderRepository.insert(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        final var result = salesService.placeOrder(userId, "addr-123", "COUPON", "paytech");

        // Assert
        assertNotNull(result);
        verify(orderRepository).insert(any(Order.class));
        verify(cartService).clearCart(userId);
        verify(paymentPort).initiate(any(), any(), any(), any());
    }
}
