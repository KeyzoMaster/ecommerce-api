package com.lemzo.ecommerce.util.currency;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Service par défaut pour la conversion de devises.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class DefaultCurrencyConverter implements CurrencyConverter {

    private final ExchangeRateProvider rateProvider;

    @Override
    public BigDecimal convert(final BigDecimal amount, final Currency from, final Currency target) {
        final Currency source = Objects.requireNonNull(from, "La devise source est requise.");
        final Currency destination = Objects.requireNonNull(target, "La devise cible est requise.");

        return Optional.ofNullable(amount)
                .map(amt -> source.equals(destination) ? amt : performConversion(amt, source, destination))
                .orElseThrow(() -> new IllegalArgumentException("Le montant ne peut pas être nul."));
    }

    private BigDecimal performConversion(final BigDecimal amount, final Currency from, final Currency target) {
        final BigDecimal rate = rateProvider.getExchangeRate(from, target);
        final BigDecimal converted = amount.multiply(rate);
        return converted.setScale(target.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    }
}
