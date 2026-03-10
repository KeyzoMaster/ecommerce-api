package com.lemzo.ecommerce.iam.infrastructure.security;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.UUID;

@ApplicationScoped
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class UserOwnershipProvider implements OwnershipProvider {

    @Override
    public ResourceType getResourceType() {
        return ResourceType.USER;
    }

    @Override
    public UUID getOwnerId(final UUID resourceId) {
        return resourceId;
    }
}
