package com.lemzo.ecommerce.domain.shipping.service;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.core.domain.shipping.ShippingMethod;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import com.lemzo.ecommerce.domain.sales.service.ShippingRateProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation réaliste et simple des frais de port.
 * Basé sur le poids total, la destination et le montant de la commande.
 */
@ApplicationScoped
public class DefaultShippingRateProvider implements ShippingRateProvider {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50000");
    private static final BigDecimal RATE_PER_KG = new BigDecimal("500");

    @Override
    public BigDecimal calculateRate(UUID storeId, Address destination, ShippingMethod method, BigDecimal orderAmount, List<OrderItem> items) {
        
        // 1. Règle de gratuité simple
        if (orderAmount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }

        // 2. Calcul du poids total
        BigDecimal totalWeight = Optional.ofNullable(items).orElse(List.of()).stream()
                .map(item -> Optional.ofNullable(item.getWeight()).orElse(BigDecimal.ZERO)
                        .multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Tarif de base selon le mode
        BigDecimal baseRate = switch (method) {
            case EXPRESS -> new BigDecimal("5000");
            case STANDARD -> new BigDecimal("2000");
            case RELAY -> new BigDecimal("1500");
            case PICKUP -> BigDecimal.ZERO;
        };

        if (method == ShippingMethod.PICKUP) return BigDecimal.ZERO;

        // 4. Majoration par zone (simplifiée)
        BigDecimal zoneMultiplier = isLocal(destination) ? BigDecimal.ONE : new BigDecimal("2.5");

        // Formule : (Base + (Poids * 500)) * Multiplicateur Zone
        return baseRate.add(totalWeight.multiply(RATE_PER_KG)).multiply(zoneMultiplier);
    }

    private boolean isLocal(Address address) {
        return Optional.ofNullable(address)
                .map(Address::getCountry)
                .map(c -> c.equalsIgnoreCase("Sénégal") || c.equalsIgnoreCase("Senegal"))
                .orElse(true);
    }
}
