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
import java.util.Set;
import java.util.logging.Logger;

/**
 * Seeder pour les utilisateurs et la configuration PBAC initiale.
 */
@ApplicationScoped
public class IamSeeder implements DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(IamSeeder.class.getName());

    @Inject
    private UserService userService;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private PermissionRepository permissionRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    @Transactional
    public void seed() {
        // 1. Assurer l'existence des permissions nécessaires
        Permission platManage = getOrCreatePermission(ResourceType.PLATFORM, PbacAction.MANAGE);
        Permission catRead = getOrCreatePermission(ResourceType.CATALOG, PbacAction.READ);
        Permission catCreate = getOrCreatePermission(ResourceType.CATALOG, PbacAction.CREATE);
        Permission catUpdate = getOrCreatePermission(ResourceType.CATALOG, PbacAction.UPDATE);
        Permission prodUpdate = getOrCreatePermission(ResourceType.PRODUCT, PbacAction.UPDATE);
        Permission salesRead = getOrCreatePermission(ResourceType.SALES, PbacAction.READ);
        Permission salesManage = getOrCreatePermission(ResourceType.SALES, PbacAction.MANAGE);
        Permission orderCreate = getOrCreatePermission(ResourceType.ORDER, PbacAction.CREATE);
        Permission orderRead = getOrCreatePermission(ResourceType.ORDER, PbacAction.READ);
        Permission viewStats = getOrCreatePermission(ResourceType.ANALYTICS, PbacAction.VIEW_ANALYTICS);
        Permission applyCoupon = getOrCreatePermission(ResourceType.MARKETING, PbacAction.APPLY_COUPON);

        // 2. Assurer l'existence des rôles et affecter les permissions
        Role adminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> roleRepository.insert(new Role("SUPER_ADMIN", "Administrateur système")));
        adminRole.getPermissions().add(platManage);
        roleRepository.save(adminRole);
        
        Role ownerRole = roleRepository.findByName("STORE_OWNER")
                .orElseGet(() -> roleRepository.insert(new Role("STORE_OWNER", "Propriétaire de boutique")));
        ownerRole.getPermissions().addAll(Set.of(catRead, catCreate, catUpdate, prodUpdate, salesRead, salesManage, viewStats));
        roleRepository.save(ownerRole);
        
        Role clientRole = roleRepository.findByName("CLIENT")
                .orElseGet(() -> roleRepository.insert(new Role("CLIENT", "Client standard")));
        clientRole.getPermissions().addAll(Set.of(catRead, orderCreate, orderRead, applyCoupon));
        roleRepository.save(clientRole);

        // 3. Création des utilisateurs de test
        createIfMissing("admin", "admin@ecommerce.local", "admin123", adminRole);
        createIfMissing("owner", "owner@ecommerce.local", "owner123", ownerRole);
        createIfMissing("client", "client@ecommerce.local", "client123", clientRole);
    }

    private Permission getOrCreatePermission(ResourceType resource, PbacAction action) {
        return permissionRepository.findByResourceTypeAndAction(resource, action)
                .orElseGet(() -> permissionRepository.insert(new Permission(resource, action)));
    }

    private void createIfMissing(String username, String email, String password, Role role) {
        if (userService.findByIdentifier(username).isEmpty()) {
            LOGGER.info("Seeding user: " + username + " with role: " + role.getName());
            User user = userService.createUser(username, email, password);
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    @Override
    public int priority() {
        return 1;
    }
}
