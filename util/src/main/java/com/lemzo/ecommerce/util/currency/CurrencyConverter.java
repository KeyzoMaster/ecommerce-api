package com.lemzo.ecommerce.util.currency;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Interface pour la conversion de devises.
 */
public interface CurrencyConverter {

    /**
     * Convertit un montant d'une devise source vers une devise cible.
     */
    BigDecimal convert(BigDecimal amount, Currency from, Currency to);
}
