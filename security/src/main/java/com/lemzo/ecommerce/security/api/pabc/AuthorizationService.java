package com.lemzo.ecommerce.security.api.pabc;

import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.security.infrastructure.pabc.UserPrincipal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Optional;
import java.util.UUID;

/**
 * Service central pour les vérifications de permissions programmatiques.
 * Strictement conforme au mandat : Zéro comparaison nulle manuelle.
 */
@ApplicationScoped
public class AuthorizationService {

    @Inject
    private Instance<OwnershipProvider> ownershipProviders;

    /**
     * Vérifie si l'utilisateur actuel a une permission spécifique.
     */
    public boolean isAuthorized(SecurityContext securityContext, ResourceType resource, PbacAction action) {
        return isAuthorized(securityContext, resource, action, null);
    }

    /**
     * Vérifie si l'utilisateur actuel a une permission, avec check d'ownership optionnel.
     */
    public boolean isAuthorized(SecurityContext securityContext, ResourceType resource, PbacAction action, UUID targetId) {
        return Optional.ofNullable(securityContext)
                .map(SecurityContext::getUserPrincipal)
                .filter(UserPrincipal.class::isInstance)
                .map(UserPrincipal.class::cast)
                .filter(principal -> hasPbacPermission(principal, resource, action))
                .map(principal -> Optional.ofNullable(targetId)
                        .filter(id -> !principal.getPermissions().contains("platform:manage"))
                        .map(id -> isOwner(principal, resource, id))
                        .orElse(true)) // Si targetId est absent, le PBAC suffit
                .orElse(false);
    }

    private boolean hasPbacPermission(UserPrincipal principal, ResourceType resource, PbacAction action) {
        return Optional.of(principal)
                .filter(p -> p.getPermissions().contains("platform:manage"))
                .map(p -> true)
                .orElseGet(() -> checkRecursivePermission(principal, resource, action));
    }

    private boolean checkRecursivePermission(UserPrincipal principal, ResourceType resource, PbacAction action) {
        boolean hasDirectOrImplicit = action.getGrantingActions().stream()
                .map(grantingAction -> String.format("%s:%s", resource.name().toLowerCase(), grantingAction.name().toLowerCase()))
                .anyMatch(slug -> principal.getPermissions().contains(slug));

        return hasDirectOrImplicit || Optional.ofNullable(resource.getParent())
                .map(parent -> checkRecursivePermission(principal, parent, action))
                .orElse(false);
    }

    private boolean isOwner(UserPrincipal principal, ResourceType resource, UUID targetId) {
        return ownershipProviders.stream()
                .filter(p -> p.getResourceType() == resource)
                .findFirst()
                .map(p -> p.getOwnerId(targetId))
                .map(ownerId -> ownerId.equals(principal.getUserId()))
                .orElse(false);
    }
}
