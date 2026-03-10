package com.lemzo.ecommerce.domain.inventory.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import com.lemzo.ecommerce.domain.core.catalog.CatalogPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Fournit l'appartenance pour l'inventaire.
 * L'identifiant cible pour l'inventaire est généralement l'identifiant du produit.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class InventoryOwnershipProvider implements OwnershipProvider {

    private final CatalogPort catalogPort;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.INVENTORY;
    }

    @Override
    public UUID getOwnerId(final UUID resourceId) {
        return null;
    }

    @Override
    public UUID getParentId(final UUID resourceId) {
        return catalogPort.getProductStoreId(resourceId).orElse(null);
    }
}
