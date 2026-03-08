package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.exception.ResourceConflictException;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.iam.api.dto.RoleCreateRequest;
import com.lemzo.ecommerce.iam.domain.Permission;
import com.lemzo.ecommerce.iam.domain.Role;
import com.lemzo.ecommerce.iam.repository.PermissionRepository;
import com.lemzo.ecommerce.iam.repository.RoleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service de gestion des rôles et permissions.
 * Inspiré de proevalis-backend pour la validation de sécurité.
 */
@ApplicationScoped
public class RoleManagementService {

    private static final Logger LOGGER = Logger.getLogger(RoleManagementService.class.getName());

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private PermissionRepository permissionRepository;

    @Context
    private SecurityContext securityContext;

    /**
     * Crée un nouveau rôle avec validation contre l'escalade de privilèges.
     */
    @Transactional
    @Audit(action = "ROLE_CREATE")
    public Role createRole(RoleCreateRequest request) {
        roleRepository.findByName(request.name()).ifPresent(r -> {
            throw new ResourceConflictException("Le rôle existe déjà : " + request.name());
        });

        Set<Permission> permissions = request.permissionIds().stream()
                .map(id -> permissionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée : " + id)))
                .collect(Collectors.toSet());

        validateNoPrivilegeEscalation(permissions);

        Role role = new Role(request.name(), request.description());
        role.setPermissions(permissions);
        
        return roleRepository.insert(role);
    }

    /**
     * Vérifie que l'utilisateur ne tente pas d'accorder des droits qu'il ne possède pas lui-même.
     */
    private void validateNoPrivilegeEscalation(Set<Permission> requestedPermissions) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        
        // Super Admin bypass
        if (principal.getPermissions().contains("platform:manage")) {
            return;
        }

        requestedPermissions.stream()
                .map(Permission::getSlug)
                .filter(slug -> !principal.getPermissions().contains(slug))
                .findFirst()
                .ifPresent(slug -> {
                    LOGGER.warning("Tentative d'escalade de privilège par " + principal.getName() + " pour : " + slug);
                    throw new BusinessRuleException("error.iam.privilege_escalation");
                });
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
