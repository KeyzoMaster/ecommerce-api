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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentGatewayProvider Unit Tests (JUnit 6)")
class PaymentGatewayProviderTest {

    @Mock
    private Instance<PaymentPort> gateways;

    @InjectMocks
    private PaymentGatewayProvider provider;

    @Test
    @DisplayName("Should return the correct gateway for a valid provider name")
    void shouldReturnCorrectGateway() {
        // Arrange
        String providerName = "paytech";
        PaymentPort mockPaytech = mock(PaymentPort.class);
        Instance<PaymentPort> selectedInstance = mock(Instance.class);

        when(gateways.select(any(NamedLiteral.class))).thenReturn(selectedInstance);
        when(selectedInstance.isUnsatisfied()).thenReturn(false);
        when(selectedInstance.get()).thenReturn(mockPaytech);

        // Act
        PaymentPort result = provider.getGateway(providerName);

        // Assert
        assertNotNull(result);
        assertEquals(mockPaytech, result);
        verify(gateways).select(eq(NamedLiteral.of("paytech")));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for an unknown provider")
    void shouldThrowExceptionForUnknownProvider() {
        // Arrange
        String unknown = "unknown_pay";
        Instance<PaymentPort> selectedInstance = mock(Instance.class);

        when(gateways.select(any(NamedLiteral.class))).thenReturn(selectedInstance);
        when(selectedInstance.isUnsatisfied()).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> provider.getGateway(unknown));
    }

    @Test
    @DisplayName("Should handle case-insensitive provider names")
    void shouldHandleCaseInsensitivity() {
        // Arrange
        String providerName = "Paypal";
        PaymentPort mockPaypal = mock(PaymentPort.class);
        Instance<PaymentPort> selectedInstance = mock(Instance.class);

        when(gateways.select(any(NamedLiteral.class))).thenReturn(selectedInstance);
        when(selectedInstance.isUnsatisfied()).thenReturn(false);
        when(selectedInstance.get()).thenReturn(mockPaypal);

        // Act
        provider.getGateway(providerName);

        // Assert
        verify(gateways).select(eq(NamedLiteral.of("paypal")));
    }
}
