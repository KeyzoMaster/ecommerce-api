package com.lemzo.ecommerce.payment.infrastructure.cod;

import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import com.lemzo.ecommerce.core.contract.payment.PaymentResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Gestion du Paiement à la livraison (Cash on Delivery).
 */
@ApplicationScoped
@Named("cod")
public class CodAdapter implements PaymentPort {

    public CodAdapter() {
        // Constructeur explicite
    }

    @Override
    public PaymentResult initiate(final BigDecimal amount, final String currency, final String orderId, final String description) {
        final var ref = "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(ref, null);
    }

    @Override
    public PaymentResult verify(final String transactionId) {
        // En COD, on considère que c'est en attente jusqu'à livraison physique
        return PaymentResult.success(transactionId, null);
    }
}
