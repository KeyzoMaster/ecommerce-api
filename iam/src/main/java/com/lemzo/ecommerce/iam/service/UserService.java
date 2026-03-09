package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.domain.core.iam.UserPort;
import com.lemzo.ecommerce.iam.domain.Permission;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.domain.UserFactory;
import com.lemzo.ecommerce.iam.repository.UserRepository;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
import com.lemzo.ecommerce.core.annotation.Audit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service de gestion des utilisateurs implémentant le Port du domaine.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class UserService implements UserPort {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordService passwordService;

    @Override
    public Optional<User> findById(final UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<Address> findAddressById(final UUID userId, final String addressId) {
        return findById(userId)
                .flatMap(user -> user.getAddresses().stream()
                        .filter(addr -> addressId.equals(addr.getTechnicalId()))
                        .findFirst());
    }

    @Override
    public Set<String> getPermissions(final UUID userId) {
        return findById(userId)
                .map(user -> Stream.concat(
                        user.getRoles().stream().flatMap(role -> role.getPermissions().stream()),
                        user.getAdhocPermissions().stream()
                )
                .map(Permission::getSlug)
                .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    @Override
    public boolean canAccessStore(final UUID userId, final UUID storeId) {
        final boolean hasManagePermission = getPermissions(userId).contains("platform:manage");
        
        final boolean isOwner = storeRepository.findById(storeId)
                .map(store -> store.getOwner().getId().equals(userId))
                .orElse(false);

        return hasManagePermission || isOwner;
    }

    @Transactional
    public User register(final String username, final String email, final String plainPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessRuleException("error.iam.username_taken");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessRuleException("error.iam.email_taken");
        }

        final String hashedPassword = passwordService.hash(plainPassword.toCharArray());
        final User user = UserFactory.create(username, email, hashedPassword);
        
        return userRepository.insert(user);
    }

    public Optional<User> findByIdentifier(final String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier));
    }

    @Transactional
    @Audit(action = "USER_PROFILE_UPDATE")
    public User updateProfile(final UUID userId, final String firstName, final String lastName) {
        final User user = findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        return userRepository.update(user);
    }

    @Transactional
    @Audit(action = "USER_PAYMENT_METHOD_ADD")
    public User addPaymentMethod(final UUID userId, final String type, final Map<String, Object> details) {
        final User user = findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        final Map<String, Object> methodDetails = new HashMap<>(details);
        methodDetails.put("type", type);
        user.getPaymentMethods().add(methodDetails);
        
        return userRepository.update(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_ADD")
    public User addAddress(final UUID userId, final Address address) {
        final User user = findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        address.setTechnicalId(Optional.ofNullable(address.getTechnicalId()).orElseGet(() -> UUID.randomUUID().toString()));
        
        user.getAddresses().add(address);
        
        return userRepository.update(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_REMOVE")
    public User removeAddress(final UUID userId, final String addressId) {
        final User user = findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.getAddresses().removeIf(addr -> addr.getTechnicalId().equals(addressId));
        
        return userRepository.update(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_UPDATE")
    public User updateAddress(final UUID userId, final String addressId, final Address updatedAddress) {
        final User user = findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));

        user.getAddresses().stream()
                .filter(addr -> addr.getTechnicalId().equals(addressId))
                .findFirst()
                .ifPresent(addr -> {
                    addr.setLabel(updatedAddress.getLabel());
                    addr.setStreet(updatedAddress.getStreet());
                    addr.setCity(updatedAddress.getCity());
                    addr.setZipCode(updatedAddress.getZipCode());
                    addr.setCountry(updatedAddress.getCountry());
                });

        return userRepository.update(user);
    }
}
