package com.lemzo.ecommerce.domain.catalog.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class CatalogOwnershipProvider implements OwnershipProvider {

    @Override
    public ResourceType getResourceType() {
        return ResourceType.CATALOG;
    }

    @Override
    public UUID getOwnerId(final UUID resourceId) {
        return null;
    }

    @Override
    public UUID getParentId(final UUID resourceId) {
        return resourceId; // The Catalog ID is the Store ID
    }
}
