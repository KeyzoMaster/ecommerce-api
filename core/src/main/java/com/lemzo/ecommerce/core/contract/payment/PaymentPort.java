package com.lemzo.ecommerce.core.contract.payment;

import java.math.BigDecimal;

/**
 * Port pour l'intégration des passerelles de paiement.
 */
public interface PaymentPort {
    /**
     * Initie un paiement auprès de la passerelle.
     */
    PaymentResult initiate(BigDecimal amount, String currency, String orderId, String description);

    /**
     * Vérifie le statut d'une transaction.
     */
    PaymentResult verify(String transactionId);
}
