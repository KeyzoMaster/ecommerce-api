package com.lemzo.ecommerce.payment.infrastructure.cod;

import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
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

    @Override
    public PaymentResult initiate(BigDecimal amount, String currency, String orderId, String description) {
        // Pour COD, il n'y a pas de redirection, on confirme l'intention immédiatement
        String ref = "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.ok(ref, null);
    }

    @Override
    public PaymentStatus getStatus(String transactionId) {
        // Le statut reste PENDING tant que le livreur n'a pas encaissé
        return PaymentStatus.PENDING;
    }

    @Override
    public PaymentResult refund(String transactionId, BigDecimal amount) {
        // Le remboursement COD est géré manuellement ou via avoir
        return PaymentResult.error("NOT_SUPPORTED", "Le remboursement automatique n'est pas supporté pour le COD");
    }
}
