package com.lemzo.ecommerce.iam.infrastructure.seeding;

import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.domain.Address;
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
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Seeder pour les données IAM (Rôles, Permissions, Utilisateurs).
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class IamSeeder implements DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(IamSeeder.class.getName());

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Override
    @Transactional
    public void seed() {
        try (Stream<Permission> existing = permissionRepository.findAll()) {
            if (existing.findAny().isPresent()) {
                LOGGER.info("IAM data already seeded. Skipping.");
                return;
            }
        }

        LOGGER.info("Seeding IAM data...");

        // 1. Créer toutes les permissions possibles
        final Set<Permission> allPermissions = Stream.of(ResourceType.values())
                .flatMap(res -> Stream.of(PbacAction.values())
                        .map(act -> new Permission(res, act)))
                .map(permissionRepository::insert)
                .collect(Collectors.toSet());

        // 2. Créer les Rôles standards
        final Role adminRole = createRole("ADMIN", "Administrateur système", allPermissions);
        
        final Set<Permission> clientPermissions = filterPermissions(allPermissions, 
                ResourceType.PRODUCT, PbacAction.READ, 
                ResourceType.ORDER, PbacAction.READ);
        final Role clientRole = createRole("CLIENT", "Client standard", clientPermissions);
        
        final Set<Permission> sellerPermissions = filterPermissions(allPermissions, 
                ResourceType.PRODUCT, PbacAction.MANAGE, 
                ResourceType.ORDER, PbacAction.READ);
        final Role sellerRole = createRole("SELLER", "Vendeur boutique", sellerPermissions);

        // 3. Créer des utilisateurs de test
        createUser("admin", "admin@ecommerce.local", "admin123", adminRole);
        createUser("client1", "client1@gmail.com", "password", clientRole);
        createUser("client2", "client2@gmail.com", "password", clientRole);
        createUser("vendeur1", "vendeur1@boutique.com", "password", sellerRole);

        LOGGER.info("IAM seeding completed.");
    }

    private Role createRole(final String name, final String desc, final Set<Permission> permissions) {
        final Role role = new Role(name, desc);
        role.setPermissions(permissions);
        return roleRepository.insert(role);
    }

    private void createUser(final String username, final String email, final String pass, final Role role) {
        final String hashedPass = passwordService.hash(pass.toCharArray());
        final User user = UserFactory.create(username, email, hashedPass);
        user.getRoles().add(role);
        
        final Address address = Address.builder()
                .technicalId("addr-" + username)
                .label("Domicile")
                .street("Rue de Test " + username)
                .city("Dakar")
                .country("Sénégal")
                .build();
        user.setAddresses(List.of(address));
        
        userRepository.insert(user);
    }

    private Set<Permission> filterPermissions(final Set<Permission> all, final Object... criteria) {
        return all.stream()
                .filter(p -> Arrays.stream(criteria)
                        .anyMatch(c -> p.getSlug().contains(c.toString().toLowerCase())))
                .collect(Collectors.toSet());
    }

    @Override
    public int priority() {
        return 1;
    }
}
