package com.lemzo.ecommerce.util.currency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour DefaultCurrencyConverter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultCurrencyConverter Unit Tests")
class DefaultCurrencyConverterTest {

    @Mock
    private ExchangeRateProvider rateProvider;

    @InjectMocks
    private DefaultCurrencyConverter converter;

    @Test
    @DisplayName("Should convert currency correctly")
    void shouldConvertSuccessfully() {
        // Arrange
        final var amount = new BigDecimal("100");
        final var usd = Currency.getInstance("USD");
        final var xof = Currency.getInstance("XOF");
        final var rate = new BigDecimal("600");

        when(rateProvider.getExchangeRate(usd, xof)).thenReturn(rate);

        // Act
        final var result = converter.convert(amount, usd, xof);

        // Assert
        assertEquals(new BigDecimal("60000").setScale(0), result);
    }

    @Test
    @DisplayName("Should return same amount if currencies are identical")
    void shouldReturnSameAmount() {
        // Arrange
        final var amount = new BigDecimal("100.50");
        final var xof = Currency.getInstance("XOF");

        // Act
        final var result = converter.convert(amount, xof, xof);

        // Assert
        assertEquals(amount, result);
    }

    @Test
    @DisplayName("Should throw exception if amount is null")
    void shouldThrowOnNullAmount() {
        final var xof = Currency.getInstance("XOF");
        assertThrows(IllegalArgumentException.class, () -> converter.convert(null, xof, xof));
    }
}
