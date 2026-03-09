package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.domain.Store;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
import com.lemzo.ecommerce.core.annotation.Audit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des boutiques.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    @Audit(action = "STORE_CREATE")
    public Store createStore(final String name, final String slug, final User owner) {
        if (storeRepository.findBySlug(slug).isPresent()) {
            throw new BusinessRuleException("error.iam.store_slug_taken");
        }
        final var store = new Store(name, slug, owner);
        return storeRepository.save(store);
    }

    public Optional<Store> findById(final UUID id) {
        return storeRepository.findById(id);
    }

    public Optional<Store> findBySlug(final String slug) {
        return storeRepository.findBySlug(slug);
    }
}
