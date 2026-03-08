package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.contract.iam.StoreAccessPort;
import com.lemzo.ecommerce.iam.domain.Store;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class StoreService implements StoreAccessPort {

    @Inject
    private StoreRepository storeRepository;

    @Override
    public Optional<UUID> getStoreOwnerId(UUID storeId) {
        return storeRepository.findById(storeId)
                .map(Store::getOwnerId);
    }

    public Optional<Store> findById(UUID id) {
        return storeRepository.findById(id);
    }
}
