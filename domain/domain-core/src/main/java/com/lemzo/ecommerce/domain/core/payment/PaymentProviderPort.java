package com.lemzo.ecommerce.domain.core.payment;

import com.lemzo.ecommerce.core.contract.payment.PaymentPort;

/**
 * Port pour obtenir une passerelle de paiement spécifique.
 */
public interface PaymentProviderPort {
    /**
     * Retourne la passerelle correspondant au nom du fournisseur.
     */
    PaymentPort getGateway(String providerName);
}
