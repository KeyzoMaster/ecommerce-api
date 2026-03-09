package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.iam.api.dto.RoleCreateRequest;
import com.lemzo.ecommerce.iam.domain.Permission;
import com.lemzo.ecommerce.iam.domain.Role;
import com.lemzo.ecommerce.iam.repository.PermissionRepository;
import com.lemzo.ecommerce.iam.repository.RoleRepository;
import com.lemzo.ecommerce.core.annotation.Audit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service de gestion des rôles et permissions.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional
    @Audit(action = "ROLE_CREATE")
    public Role createRole(final RoleCreateRequest request) {
        if (roleRepository.findByName(request.name()).isPresent()) {
            throw new BusinessRuleException("error.iam.role_already_exists");
        }

        final Set<Permission> permissions = request.permissionSlugs().stream()
                .map(this::findPermissionBySlug)
                .collect(Collectors.toSet());

        final Role role = new Role(request.name(), request.description());
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    private Permission findPermissionBySlug(final String slug) {
        final String[] parts = slug.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format de permission invalide : " + slug);
        }
        try {
            final ResourceType type = ResourceType.valueOf(parts[0].toUpperCase());
            final PbacAction action = PbacAction.valueOf(parts[1].toUpperCase());
            return permissionRepository.findByResourceTypeAndAction(type, action)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée : " + slug));
        } catch (final IllegalArgumentException exception) {
            throw new ResourceNotFoundException("Type ou Action invalide dans le slug : " + slug, exception);
        }
    }

    public Set<String> getPermissionsForUser(final com.lemzo.ecommerce.iam.domain.User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getSlug)
                .collect(Collectors.toSet());
    }
}
