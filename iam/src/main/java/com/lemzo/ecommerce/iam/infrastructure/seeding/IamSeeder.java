package com.lemzo.ecommerce.iam.infrastructure.seeding;

import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import com.lemzo.ecommerce.iam.domain.Permission;
import com.lemzo.ecommerce.iam.domain.Role;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.domain.UserFactory;
import com.lemzo.ecommerce.iam.repository.PermissionRepository;
import com.lemzo.ecommerce.iam.repository.RoleRepository;
import com.lemzo.ecommerce.iam.repository.UserRepository;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Seeder initial pour les données IAM (Rôles, Permissions, Utilisateurs).
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class IamSeeder implements DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(IamSeeder.class.getName());

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordService passwordService;

    @Override
    @Transactional
    public void seed() {
        if (userRepository.count() > 0) {
            LOGGER.info("Le seeder IAM a déjà été exécuté.");
            return;
        }

        // 1. Créer les Permissions de base
        final Permission readCatalog = createPerm(ResourceType.CATALOG, PbacAction.READ);
        final Permission createCatalog = createPerm(ResourceType.CATALOG, PbacAction.CREATE);
        final Permission viewAnalytics = createPerm(ResourceType.PLATFORM, PbacAction.VIEW_ANALYTICS);
        final Permission manageSales = createPerm(ResourceType.SALES, PbacAction.MANAGE);

        // 2. Créer les Rôles
        final Role adminRole = new Role("SUPER_ADMIN", "Administrateur système");
        adminRole.setPermissions(new HashSet<>(Arrays.asList(readCatalog, createCatalog, viewAnalytics, manageSales)));
        roleRepository.insert(adminRole);

        final Role clientRole = new Role("CLIENT", "Client standard");
        clientRole.setPermissions(new HashSet<>(Arrays.asList(readCatalog)));
        roleRepository.insert(clientRole);

        // 3. Créer les Utilisateurs par défaut
        createUser("admin", "admin@ecommerce.local", "admin123", adminRole);
        createUser("client", "client@ecommerce.local", "client123", clientRole);
        createUser("owner", "owner@ecommerce.local", "owner123", clientRole); // Sera promu manuellement via SQL ou API

        LOGGER.info("Seeding IAM terminé avec succès.");
    }

    private Permission createPerm(final ResourceType res, final PbacAction action) {
        return permissionRepository.insert(new Permission(res, action));
    }

    private void createUser(final String username, final String email, final String pass, final Role role) {
        final String hashed = passwordService.hash(pass.toCharArray());
        final User user = UserFactory.create(username, email, hashed);
        user.getRoles().add(role);
        userRepository.insert(user);
    }

    @Override
    public int priority() {
        return 1;
    }
}
