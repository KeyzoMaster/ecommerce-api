package com.lemzo.ecommerce.security.api.pabc;

import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import com.lemzo.ecommerce.domain.core.iam.UserPort;
import com.lemzo.ecommerce.security.infrastructure.pabc.UserPrincipal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service central pour l'évaluation des permissions Hybrid PBAC.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuthorizationService {

    private final Instance<OwnershipProvider> ownershipProviders;
    private final UserPort userPort;

    public boolean isAuthorized(final SecurityContext securityContext, final ResourceType resource, final PbacAction action) {
        return isAuthorized(securityContext, resource, action, null);
    }

    public boolean isAuthorized(final SecurityContext securityContext, final ResourceType resource, 
                                final PbacAction action, final UUID targetId) {
        
        return Optional.ofNullable(securityContext.getUserPrincipal())
                .filter(UserPrincipal.class::isInstance)
                .map(UserPrincipal.class::cast)
                .map(principal -> evaluate(principal, resource, action, targetId))
                .orElse(false);
    }

    private boolean evaluate(final UserPrincipal principal, final ResourceType resource, 
                             final PbacAction action, final UUID targetId) {
        
        final Set<String> possessedPermissions = userPort.getPermissions(principal.getUserId());

        if (possessedPermissions.contains("platform:manage")) {
            return true;
        }

        if (!hasPbacPermission(possessedPermissions, resource, action)) {
            return false;
        }

        return Optional.ofNullable(targetId)
                .map(id -> isOwner(principal.getUserId(), resource, id))
                .orElse(true);
    }

    private boolean hasPbacPermission(final Set<String> possessedPermissions, final ResourceType resource, final PbacAction action) {
        final Set<PbacAction> requiredActions = action.getRequiredPossessedActions();
        
        return Stream.iterate(resource, Objects::nonNull, ResourceType::getParent)
                .anyMatch(res -> requiredActions.stream()
                        .anyMatch(act -> possessedPermissions.contains(res.name().toLowerCase() + ":" + act.name().toLowerCase())));
    }

    private boolean isOwner(final UUID userId, final ResourceType resource, final UUID targetId) {
        return Optional.ofNullable(targetId)
            .map(id -> ownershipProviders.stream()
                .filter(provider -> provider.getResourceType() == resource)
                .findFirst()
                .map(provider -> {
                    final boolean isDirectOwner = Optional.ofNullable(provider.getOwnerId(id))
                        .map(userId::equals)
                        .orElse(false);

                    if (isDirectOwner) {
                        return true;
                    }

                    return Optional.ofNullable(provider.getParentId(id))
                        .flatMap(parentId -> Optional.ofNullable(resource.getParent())
                            .map(parentResource -> isOwner(userId, parentResource, parentId)))
                        .orElse(false);
                })
                .orElse(false))
            .orElse(true);
    }
}
