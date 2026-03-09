package com.lemzo.ecommerce.domain.catalog.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import com.lemzo.ecommerce.core.entity.AbstractEntity;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

/**
 * Fournit l'appartenance pour les produits.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ProductOwnershipProvider implements OwnershipProvider {

    private final ProductRepository productRepository;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.PRODUCT;
    }

    @Override
    public UUID getOwnerId(final UUID resourceId) {
        return productRepository.findById(resourceId)
                .map(Product::getCategory)
                .map(AbstractEntity::getId)
                .orElse(null);
    }
}
