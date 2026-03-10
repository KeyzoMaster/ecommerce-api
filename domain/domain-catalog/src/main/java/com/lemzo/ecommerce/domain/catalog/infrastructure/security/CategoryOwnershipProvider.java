package com.lemzo.ecommerce.domain.catalog.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class CategoryOwnershipProvider implements OwnershipProvider {

    @Override
    public ResourceType getResourceType() {
        return ResourceType.CATEGORY;
    }

    @Override
    public UUID getOwnerId(final UUID resourceId) {
        return null;
    }

    @Override
    public UUID getParentId(final UUID resourceId) {
        return null; // Categories apply to the whole catalog, no specific store ownership unless changed in requirements.
    }
}
