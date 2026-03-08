package com.lemzo.ecommerce.domain.sales.service;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.core.domain.shipping.ShippingMethod;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Contrat pour le calcul dynamique des frais de port.
 * Implémenté par le module shipping.
 */
public interface ShippingRateProvider {
    
    /**
     * Calcule les frais de port pour un vendeur, une destination et un mode donnés.
     */
    BigDecimal calculateRate(UUID storeId, Address destination, ShippingMethod method, BigDecimal orderAmount, List<OrderItem> items);
}
