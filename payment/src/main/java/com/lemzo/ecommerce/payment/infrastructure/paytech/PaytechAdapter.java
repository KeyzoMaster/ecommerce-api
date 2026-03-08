package com.lemzo.ecommerce.payment.infrastructure.paytech;

import com.lemzo.ecommerce.core.contract.payment.PaymentPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulateur d'agrégateur Paytech (Sénégal).
 * Supporte Wave, Orange Money, Free Money via une seule API.
 */
@ApplicationScoped
@Named("paytech")
public class PaytechAdapter implements PaymentPort {

    @Override
    public PaymentResult initiate(BigDecimal amount, String currency, String orderId, String description) {
        // Simulation d'une session Paytech
        String token = UUID.randomUUID().toString().substring(0, 12);
        String mockRedirectUrl = "https://paytech.sn/checkout/" + token;
        
        return PaymentResult.ok(token, mockRedirectUrl);
    }

    @Override
    public PaymentStatus getStatus(String transactionId) {
        // Dans une vraie implémentation, on interrogerait l'API Paytech
        return PaymentStatus.SUCCESS;
    }

    @Override
    public PaymentResult refund(String transactionId, BigDecimal amount) {
        return PaymentResult.ok("pt_ref_" + UUID.randomUUID().toString().substring(0, 8), null);
    }
}
