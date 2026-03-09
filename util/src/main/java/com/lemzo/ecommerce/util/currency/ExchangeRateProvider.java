package com.lemzo.ecommerce.util.currency;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Fournisseur de taux de change.
 */
@FunctionalInterface
public interface ExchangeRateProvider {
    /**
     * Retourne le taux de change entre deux devises.
     */
    BigDecimal getExchangeRate(final Currency from, final Currency to);
}
