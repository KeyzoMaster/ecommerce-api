package com.lemzo.ecommerce.domain.shipping.service;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.domain.core.sales.OrderLineInfo;
import com.lemzo.ecommerce.domain.core.shipping.ShippingMethod;
import com.lemzo.ecommerce.domain.core.shipping.ShippingRateProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.List;

/**
 * Implémentation par défaut du calcul des frais de port.
 */
@ApplicationScoped
public class DefaultShippingRateProvider implements ShippingRateProvider {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50000");

    public DefaultShippingRateProvider() {
        // Required by CDI
    }

    @Override
    public BigDecimal calculateRate(final Address address, final ShippingMethod method, 
                                    final BigDecimal itemsTotal, final List<OrderLineInfo> items) {
        
        if (itemsTotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }

        final BigDecimal baseRate = "Sénégal".equalsIgnoreCase(address.getCountry()) 
                ? new BigDecimal("2000") 
                : new BigDecimal("15000");

        return switch (method) {
            case EXPRESS -> baseRate.add(new BigDecimal("3000"));
            case PICKUP -> BigDecimal.ZERO;
            default -> baseRate;
        };
    }
}
