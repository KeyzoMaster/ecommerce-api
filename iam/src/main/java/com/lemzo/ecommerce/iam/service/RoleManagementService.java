package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.api.dto.RoleCreateRequest;
import com.lemzo.ecommerce.iam.domain.Permission;
import com.lemzo.ecommerce.iam.domain.Role;
import com.lemzo.ecommerce.iam.repository.PermissionRepository;
import com.lemzo.ecommerce.iam.repository.RoleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
        try (var roles = roleRepository.findAll()) {
            return roles.collect(Collectors.toList());
        }
    }

    public Optional<Role> getRoleById(final UUID id) {
        return roleRepository.findById(id);
    }

    public List<Permission> getAllPermissions() {
        try (var permissions = permissionRepository.findAll()) {
            return permissions.collect(Collectors.toList());
        }
    }

    @Transactional
    public Role createRole(final RoleCreateRequest request) {
        if (roleRepository.findByName(request.name()).isPresent()) {
            throw new BusinessRuleException("Le rôle " + request.name() + " existe déjà");
        }
        return createRoleFromSlugs(request.name(), request.description(), request.permissionSlugs());
    }

    @Transactional
    public Role createRole(final String name, final String description, final List<UUID> permissionIds) {
        final Role role = new Role(name, description);
        
        final var permissions = permissionIds.stream()
                .map(permissionRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
                
        role.setPermissions(permissions);
        return roleRepository.insert(role);
    }

    @Transactional
    public Role createRoleFromSlugs(final String name, final String description, final Set<String> slugs) {
        final Role role = new Role(name, description);
        
        try (var allPerms = permissionRepository.findAll()) {
            final var permissions = allPerms
                    .filter(p -> slugs.contains(p.getSlug()))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        
        return roleRepository.insert(role);
    }
}
