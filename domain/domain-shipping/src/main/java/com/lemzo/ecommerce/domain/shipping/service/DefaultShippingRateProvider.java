package com.lemzo.ecommerce.domain.shipping.service;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.core.domain.shipping.ShippingMethod;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import com.lemzo.ecommerce.domain.sales.service.ShippingRateProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation par défaut du calcul des frais de port.
 */
@ApplicationScoped
public class DefaultShippingRateProvider implements ShippingRateProvider {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50000");

    public DefaultShippingRateProvider() {
        // Constructeur explicite
    }

    @Override
    public BigDecimal calculateRate(final Address destination, final ShippingMethod method, 
                                    final BigDecimal orderAmount, final List<OrderItem> items) {
        
        if (orderAmount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }

        final var totalWeight = items.stream()
                .map(item -> Optional.ofNullable(item.getWeight()).orElse(BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var baseRate = method == ShippingMethod.EXPRESS ? new BigDecimal("2500") : new BigDecimal("1000");
        
        // Supplément poids
        final var weightSurcharge = totalWeight.multiply(new BigDecimal("100"));

        // Multiplicateur zone (simplifié)
        final var zoneMultiplier = "Sénégal".equalsIgnoreCase(destination.getCountry()) ? BigDecimal.ONE : new BigDecimal("2.5");

        return baseRate.add(weightSurcharge).multiply(zoneMultiplier);
    }
}
