package com.lemzo.ecommerce.iam.infrastructure.seeding;

import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.iam.domain.Permission;
import com.lemzo.ecommerce.iam.domain.Role;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.repository.PermissionRepository;
import com.lemzo.ecommerce.iam.repository.RoleRepository;
import com.lemzo.ecommerce.iam.repository.UserRepository;
import com.lemzo.ecommerce.iam.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Seeder pour les utilisateurs et la configuration PBAC initiale.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class IamSeeder implements DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(IamSeeder.class.getName());

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void seed() {
        // 1. Assurer l'existence des permissions nécessaires
        final var platManage = getOrCreatePermission(ResourceType.PLATFORM, PbacAction.MANAGE);
        final var storeCreate = getOrCreatePermission(ResourceType.STORE, PbacAction.CREATE);
        final var catRead = getOrCreatePermission(ResourceType.CATALOG, PbacAction.READ);
        final var catCreate = getOrCreatePermission(ResourceType.CATALOG, PbacAction.CREATE);
        final var catUpdate = getOrCreatePermission(ResourceType.CATALOG, PbacAction.UPDATE);
        final var prodUpdate = getOrCreatePermission(ResourceType.PRODUCT, PbacAction.UPDATE);
        final var salesRead = getOrCreatePermission(ResourceType.SALES, PbacAction.READ);
        final var salesManage = getOrCreatePermission(ResourceType.SALES, PbacAction.MANAGE);
        final var orderCreate = getOrCreatePermission(ResourceType.ORDER, PbacAction.CREATE);
        final var orderRead = getOrCreatePermission(ResourceType.ORDER, PbacAction.READ);
        final var viewStats = getOrCreatePermission(ResourceType.ANALYTICS, PbacAction.VIEW_ANALYTICS);
        final var applyCoupon = getOrCreatePermission(ResourceType.MARKETING, PbacAction.APPLY_COUPON);

        // 2. Assurer l'existence des rôles et affecter les permissions
        final var adminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> roleRepository.insert(new Role("SUPER_ADMIN", "Administrateur système")));
        adminRole.getPermissions().add(platManage);
        roleRepository.save(adminRole);
        
        final var ownerRole = roleRepository.findByName("STORE_OWNER")
                .orElseGet(() -> roleRepository.insert(new Role("STORE_OWNER", "Propriétaire de boutique")));
        ownerRole.getPermissions().addAll(Set.of(catRead, catCreate, catUpdate, prodUpdate, salesRead, salesManage, viewStats, storeCreate));
        roleRepository.save(ownerRole);
        
        final var clientRole = roleRepository.findByName("CLIENT")
                .orElseGet(() -> roleRepository.insert(new Role("CLIENT", "Client standard")));
        clientRole.getPermissions().addAll(Set.of(catRead, orderCreate, orderRead, applyCoupon));
        roleRepository.save(clientRole);

        // 3. Création des utilisateurs de test
        createIfMissing("admin", "admin@ecommerce.local", "admin123", adminRole);
        createIfMissing("owner", "owner@ecommerce.local", "owner123", ownerRole);
        createIfMissing("client", "client@ecommerce.local", "client123", clientRole);
    }

    private Permission getOrCreatePermission(final ResourceType resource, final PbacAction action) {
        return permissionRepository.findByResourceTypeAndAction(resource, action)
                .orElseGet(() -> permissionRepository.insert(new Permission(resource, action)));
    }

    private void createIfMissing(final String username, final String email, final String password, final Role role) {
        if (userService.findByIdentifier(username).isEmpty()) {
            LOGGER.info(() -> "Seeding user: " + username + " with role: " + role.getName());
            final var user = userService.register(username, email, password);
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    @Override
    public int priority() {
        return 1;
    }
}
