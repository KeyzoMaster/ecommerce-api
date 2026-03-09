package com.lemzo.ecommerce.payment.service;

import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import com.lemzo.ecommerce.domain.core.payment.PaymentProviderPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

/**
 * Implémentation du fournisseur de passerelles de paiement.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PaymentGatewayProvider implements PaymentProviderPort {

    private final Instance<PaymentPort> gateways;

    @Override
    public PaymentPort getGateway(final String providerName) {
        final String name = Optional.ofNullable(providerName)
                .map(String::toLowerCase)
                .orElse("cod");

        final Instance<PaymentPort> selected = gateways.select(NamedLiteral.of(name));
        
        if (selected.isResolvable()) {
            return selected.get();
        }
        
        throw new IllegalArgumentException("Passerelle de paiement non supportée : " + providerName);
    }
}
