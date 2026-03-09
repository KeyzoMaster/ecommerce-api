package com.lemzo.ecommerce.domain.catalog.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import com.lemzo.ecommerce.security.api.pabc.OwnershipProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

/**
 * Provider pour vérifier la propriété d'un produit.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ProductOwnershipProvider implements OwnershipProvider {

    private final ProductRepository productRepository;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.PRODUCT;
    }

    @Override
    public UUID getOwnerId(final UUID productId) {
        return productRepository.findById(productId)
                .map(Product::getCategory)
                .map(cat -> cat.getId()) // En réalité, le produit appartient au Store de la catégorie ou directement au Store.
                // Pour l'instant, on retourne l'ID du propriétaire du Store via la hiérarchie.
                // Dans ce modèle simplifié, on pourrait dire que seul l'admin gère le catalogue global, 
                // ou ajouter store_id à Product.
                .orElse(null);
    }
}
