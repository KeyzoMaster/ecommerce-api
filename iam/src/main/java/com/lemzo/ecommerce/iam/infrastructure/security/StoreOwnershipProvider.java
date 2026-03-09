package com.lemzo.ecommerce.iam.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import com.lemzo.ecommerce.iam.domain.Store;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

/**
 * Fournit l'appartenance pour les boutiques.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class StoreOwnershipProvider implements OwnershipProvider {

    private final StoreRepository storeRepository;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.STORE;
    }

    @Override
    public UUID getOwnerId(final UUID resourceId) {
        return storeRepository.findById(resourceId)
                .map(Store::getOwner)
                .map(com.lemzo.ecommerce.iam.domain.User::getId)
                .orElse(null);
    }
}
