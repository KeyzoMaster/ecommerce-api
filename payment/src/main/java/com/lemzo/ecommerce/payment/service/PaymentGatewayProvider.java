package com.lemzo.ecommerce.payment.service;

import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Optional;

/**
 * Service pour fournir la passerelle de paiement appropriée.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PaymentGatewayProvider {

    private final Instance<PaymentPort> gateways;

    /**
     * Récupère un adaptateur par son nom (ex: "stripe", "paypal").
     */
    public PaymentPort getGateway(final String provider) {
        return Optional.of(gateways.select(NamedLiteral.of(provider.toLowerCase())))
                .filter(Instance::isResolvable)
                .map(Instance::get)
                .orElseThrow(() -> new IllegalArgumentException("Passerelle de paiement non supportée : " + provider));
    }
}
