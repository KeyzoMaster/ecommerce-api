package com.lemzo.ecommerce.core.contract.payment;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Port d'abstraction pour les passerelles de paiement.
 */
public interface PaymentPort {

    /**
     * Initialise une transaction de paiement.
     */
    PaymentResult initiate(BigDecimal amount, String currency, String orderId, String description);

    /**
     * Vérifie le statut d'une transaction.
     */
    PaymentStatus getStatus(String transactionId);

    /**
     * Rembourse une transaction.
     */
    PaymentResult refund(String transactionId, BigDecimal amount);

    enum PaymentStatus {
        PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED
    }

    record PaymentResult(
            boolean success,
            String transactionId,
            String redirectUrl,
            String errorCode,
            String errorMessage
    ) {
        public static PaymentResult ok(String transactionId, String redirectUrl) {
            return new PaymentResult(true, transactionId, redirectUrl, null, null);
        }

        public static PaymentResult error(String errorCode, String errorMessage) {
            return new PaymentResult(false, null, null, errorCode, errorMessage);
        }
    }
}
