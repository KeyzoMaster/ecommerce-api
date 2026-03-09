package com.lemzo.ecommerce.core.contract.payment;

import java.util.Optional;

/**
 * Résultat d'une opération de paiement.
 */
public record PaymentResult(
    boolean success,
    String transactionId,
    String redirectUrl,
    String errorCode,
    String errorMessage
) {
    public static PaymentResult success(final String transactionId, final String redirectUrl) {
        return new PaymentResult(true, transactionId, redirectUrl, null, null);
    }

    public static PaymentResult failure(final String errorCode, final String errorMessage) {
        return new PaymentResult(false, null, null, errorCode, errorMessage);
    }

    public Optional<String> getTransactionId() {
        return Optional.ofNullable(transactionId);
    }

    public Optional<String> getRedirectUrl() {
        return Optional.ofNullable(redirectUrl);
    }
}
