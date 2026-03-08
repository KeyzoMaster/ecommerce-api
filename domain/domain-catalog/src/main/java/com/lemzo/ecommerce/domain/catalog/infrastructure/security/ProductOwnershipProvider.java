package com.lemzo.ecommerce.domain.catalog.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.iam.StoreAccessPort;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import com.lemzo.ecommerce.security.api.pabc.OwnershipProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/**
 * Provider pour vérifier la propriété d'un produit.
 * Un produit appartient à l'utilisateur qui possède la boutique rattachée.
 */
@ApplicationScoped
public class ProductOwnershipProvider implements OwnershipProvider {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private StoreAccessPort storeAccessPort;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.PRODUCT;
    }

    @Override
    public UUID getOwnerId(UUID productId) {
        return productRepository.findById(productId)
                .flatMap(product -> storeAccessPort.getStoreOwnerId(product.getStoreId()))
                .orElse(null);
    }
}
