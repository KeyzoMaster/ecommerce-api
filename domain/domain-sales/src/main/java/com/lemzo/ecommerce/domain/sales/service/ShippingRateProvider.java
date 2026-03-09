package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.core.domain.shipping.ShippingMethod;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import java.math.BigDecimal;
import java.util.List;

/**
 * Contrat pour le calcul des frais de port.
 */
@FunctionalInterface
public interface ShippingRateProvider {
    /**
     * Calcule le montant des frais de port.
     */
    BigDecimal calculateRate(Address destination, ShippingMethod method, BigDecimal orderAmount, List<OrderItem> items);
}
