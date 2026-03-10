package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.iam.domain.Store;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
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
            throw new BusinessRuleException("Le slug " + slug + " est déjà utilisé");
        }
        final Store store = new Store(name, slug, owner);
        return storeRepository.insert(store);
    }


    public Optional<Store> findById(final UUID id) {
        return storeRepository.findById(id);
    }

    public Optional<Store> findByName(final String name) {
        return storeRepository.findByName(name);
    }

    @Transactional
    @Audit(action = "STORE_UPDATE")
    public Store updateStore(final UUID id, final String name, final String description) {
        final var store = findById(id)
                .orElseThrow(() -> new BusinessRuleException("error.iam.store_not_found"));
        
        store.setName(name);
        store.setDescription(description);
        return storeRepository.update(store);
    }
}
