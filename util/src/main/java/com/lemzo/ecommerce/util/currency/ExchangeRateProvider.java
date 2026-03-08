package com.lemzo.ecommerce.util.currency;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Interface pour la récupération des taux de change.
 */
public interface ExchangeRateProvider {

    /**
     * Récupère le taux de change entre deux devises.
     */
    BigDecimal getExchangeRate(Currency from, Currency to);
}
