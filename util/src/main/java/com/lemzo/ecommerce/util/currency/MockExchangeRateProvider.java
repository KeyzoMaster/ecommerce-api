package com.lemzo.ecommerce.util.currency;

import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * Implémentation de test pour les taux de change.
 */
@ApplicationScoped
public class MockExchangeRateProvider implements ExchangeRateProvider {

    @Override
    public BigDecimal getExchangeRate(Currency from, Currency to) {
        if (from.getCurrencyCode().equals("EUR") && to.getCurrencyCode().equals("USD")) {
            return new BigDecimal("1.08");
        }
        if (from.getCurrencyCode().equals("USD") && to.getCurrencyCode().equals("EUR")) {
            return new BigDecimal("0.93");
        }
        return BigDecimal.ONE;
    }
}
