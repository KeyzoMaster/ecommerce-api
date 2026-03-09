package com.lemzo.ecommerce.util.currency;

import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * Implémentation de test pour le taux de change.
 */
@ApplicationScoped
public class MockExchangeRateProvider implements ExchangeRateProvider {

    @Override
    public BigDecimal getExchangeRate(final Currency from, final Currency target) {
        if ("USD".equals(from.getCurrencyCode()) && "XOF".equals(target.getCurrencyCode())) {
            return new BigDecimal("600");
        }
        if ("XOF".equals(from.getCurrencyCode()) && "USD".equals(target.getCurrencyCode())) {
            return new BigDecimal("0.0016");
        }
        return BigDecimal.ONE;
    }
}
