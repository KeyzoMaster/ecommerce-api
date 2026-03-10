package com.lemzo.ecommerce.iam.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import com.lemzo.ecommerce.iam.domain.Store;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Fournisseur de propriété pour les boutiques.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class StoreOwnershipProvider implements OwnershipProvider {

    private final StoreRepository storeRepository;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.PLATFORM; // Les boutiques appartiennent à la plateforme
    }

    @Override
    public UUID getOwnerId(final UUID targetId) {
        return storeRepository.findById(targetId)
                .map(Store::getOwnerId)
                .orElse(null);
    }
}
