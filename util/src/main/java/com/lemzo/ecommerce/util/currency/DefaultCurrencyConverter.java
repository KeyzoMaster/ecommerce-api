package com.lemzo.ecommerce.util.currency;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;

/**
 * Service par défaut pour la conversion de devises.
 */
@ApplicationScoped
public class DefaultCurrencyConverter implements CurrencyConverter {

    @Inject
    private ExchangeRateProvider exchangeRateProvider;

    @Override
    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        Objects.requireNonNull(from, "La devise source est requise.");
        Objects.requireNonNull(to, "La devise cible est requise.");

        return Optional.ofNullable(amount)
                .map(amt -> {
                    if (from.equals(to)) {
                        return amt;
                    }

                    BigDecimal rate = exchangeRateProvider.getExchangeRate(from, to);
                    BigDecimal converted = amt.multiply(rate);

                    int fractionDigits = to.getDefaultFractionDigits();
                    return converted.setScale(fractionDigits, RoundingMode.HALF_UP);
                })
                .orElseThrow(() -> new IllegalArgumentException("Le montant ne peut pas être nul."));
    }
}
