package com.lemzo.ecommerce.domain.sales.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

/**
 * Fournit l'appartenance pour les commandes.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class OrderOwnershipProvider implements OwnershipProvider {

    private final OrderRepository orderRepository;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.ORDER;
    }

    @Override
    public UUID getOwnerId(final UUID resourceId) {
        return orderRepository.findById(resourceId)
                .map(Order::getUserId)
                .orElse(null);
    }
}
