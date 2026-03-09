package com.lemzo.ecommerce.iam.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.iam.domain.Store;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
import com.lemzo.ecommerce.security.api.pabc.OwnershipProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

/**
 * Provider pour vérifier la propriété d'une boutique.
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
    public UUID getOwnerId(final UUID storeId) {
        return storeRepository.findById(storeId)
                .map(Store::getOwnerId)
                .orElse(null);
    }
}
