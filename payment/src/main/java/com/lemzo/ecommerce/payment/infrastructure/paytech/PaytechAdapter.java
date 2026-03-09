package com.lemzo.ecommerce.payment.infrastructure.paytech;

import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import com.lemzo.ecommerce.core.contract.payment.PaymentResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulateur d'agrégateur Paytech (Sénégal).
 */
@ApplicationScoped
@Named("paytech")
public class PaytechAdapter implements PaymentPort {

    public PaytechAdapter() {
        // Constructeur explicite
    }

    @Override
    public PaymentResult initiate(final BigDecimal amount, final String currency, final String orderId, final String description) {
        final var token = UUID.randomUUID().toString().substring(0, 12);
        final var mockRedirectUrl = "https://paytech.sn/checkout/" + token;
        
        return PaymentResult.success(token, mockRedirectUrl);
    }

    @Override
    public PaymentResult verify(final String transactionId) {
        return PaymentResult.success(transactionId, null);
    }
}
