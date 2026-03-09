package com.lemzo.ecommerce.payment.service;

import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PaymentGatewayProvider.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentGatewayProvider Unit Tests")
class PaymentGatewayProviderTest {

    @Mock
    private Instance<PaymentPort> gateways;

    @InjectMocks
    private PaymentGatewayProvider provider;

    @Test
    @DisplayName("Should return the correct gateway for a valid provider name")
    void shouldReturnCorrectGateway() {
        // Arrange
        final String providerName = "paytech";
        final var mockPaytech = mock(PaymentPort.class);
        final var selectedInstance = mock(Instance.class);

        when(gateways.select(any(NamedLiteral.class))).thenReturn(selectedInstance);
        when(selectedInstance.isResolvable()).thenReturn(true);
        when(selectedInstance.get()).thenReturn(mockPaytech);

        // Act
        final var result = provider.getGateway(providerName);

        // Assert
        assertNotNull(result);
        assertEquals(mockPaytech, result);
        verify(gateways).select(eq(NamedLiteral.of("paytech")));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for an unknown provider")
    void shouldThrowExceptionForUnknownProvider() {
        // Arrange
        final String unknown = "unknown_pay";
        final var selectedInstance = mock(Instance.class);

        when(gateways.select(any(NamedLiteral.class))).thenReturn(selectedInstance);
        when(selectedInstance.isResolvable()).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> provider.getGateway(unknown));
    }

    @Test
    @DisplayName("Should handle case-insensitive provider names")
    void shouldHandleCaseInsensitivity() {
        // Arrange
        final String providerName = "Paypal";
        final var mockPaypal = mock(PaymentPort.class);
        final var selectedInstance = mock(Instance.class);

        when(gateways.select(any(NamedLiteral.class))).thenReturn(selectedInstance);
        when(selectedInstance.isResolvable()).thenReturn(true);
        when(selectedInstance.get()).thenReturn(mockPaypal);

        // Act
        provider.getGateway(providerName);

        // Assert
        verify(gateways).select(eq(NamedLiteral.of("paypal")));
    }
}
