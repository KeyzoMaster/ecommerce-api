package com.lemzo.ecommerce.domain.core.shipping;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.domain.core.sales.OrderLineInfo;
import java.math.BigDecimal;
import java.util.List;

/**
 * Port pour le calcul des frais de livraison.
 */
public interface ShippingRateProvider {
    /**
     * Calcule le montant des frais de port.
     */
    BigDecimal calculateRate(Address address, ShippingMethod method, BigDecimal itemsTotal, List<OrderLineInfo> items);
}
