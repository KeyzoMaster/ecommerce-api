package com.lemzo.ecommerce.domain.sales.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import com.lemzo.ecommerce.security.api.pabc.OwnershipProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

/**
 * Provider pour vérifier la propriété d'une commande.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class OrderOwnershipProvider implements OwnershipProvider {

    private final OrderRepository orderRepository;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.ORDER;
    }

    @Override
    public UUID getOwnerId(final UUID orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getUserId)
                .orElse(null);
    }
}
