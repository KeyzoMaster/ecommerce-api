package com.lemzo.ecommerce.payment.service;

import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Inject;
import java.util.Optional;

/**
 * Service pour fournir la passerelle de paiement appropriée.
 */
@ApplicationScoped
public class PaymentGatewayProvider {

    @Inject
    private Instance<PaymentPort> gateways;

    /**
     * Récupère un adaptateur par son nom (ex: "stripe", "paypal").
     */
    public PaymentPort getGateway(String provider) {
        Instance<PaymentPort> selected = gateways.select(NamedLiteral.of(provider.toLowerCase()));
        
        if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("Passerelle de paiement non supportée : " + provider);
        }
        
        return selected.get();
    }
}
