package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.domain.core.iam.UserPort;
import com.lemzo.ecommerce.domain.core.iam.UserDTO;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.domain.UserFactory;
import com.lemzo.ecommerce.iam.repository.UserRepository;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service de gestion des utilisateurs.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class UserService implements UserPort {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Transactional
    public User register(final String username, final String email, final String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessRuleException("error.iam.username_taken");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessRuleException("error.iam.email_taken");
        }

        final String hashedPassword = passwordService.hash(password.toCharArray());
        final User user = UserFactory.create(username, email, hashedPassword);
        
        return userRepository.insert(user);
    }

    @Override
    public Optional<?> findById(UUID userId) {
        return userRepository.findById(userId).map(u -> (Object) u);
    }

    @Override
    public Optional<UserDTO> findUserById(final UUID id) {
        return userRepository.findById(id)
                .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getEmail()));
    }

    @Override
    public Optional<? extends Object> findByIdentifier(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .map(u -> (Object) u);
    }

    @Override
    public Optional<Address> findAddressById(UUID userId, String addressId) {
        return userRepository.findById(userId)
                .flatMap(u -> u.getAddresses().stream()
                        .filter(a -> a.getTechnicalId().equals(addressId))
                        .findFirst());
    }

    @Override
    public Set<String> getPermissions(UUID userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(p -> p.getSlug())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    @Override
    public boolean canAccessStore(UUID userId, UUID storeId) {
        return true;
    }

    @Transactional
    @Audit(action = "USER_PROFILE_UPDATE")
    public User updateProfile(final UUID id, final String firstName, final String lastName) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userRepository.update(user);
    }

    @Transactional
    @Audit(action = "USER_PAYMENT_METHOD_ADD")
    public User addPaymentMethod(final UUID id, final String type, final Map<String, Object> details) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.addPaymentMethod(type, details);
        return userRepository.update(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_ADD")
    public User addAddress(final UUID id, final Address address) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        if (user.getAddresses() == null) {
            user.setAddresses(new ArrayList<>());
        }
        user.getAddresses().add(address);
        return userRepository.update(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_REMOVE")
    public User removeAddress(final UUID id, final String technicalId) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.getAddresses().removeIf(addr -> addr.getTechnicalId().equals(technicalId));
        return userRepository.update(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_UPDATE")
    public User updateAddress(final UUID id, final String technicalId, final Address updatedAddress) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.getAddresses().stream()
                .filter(addr -> addr.getTechnicalId().equals(technicalId))
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

    @Transactional
    public void updateAvatar(final UUID userId, final String avatarUrl) {
        userRepository.findById(userId)
                .ifPresent(user -> {
                    user.setFirstName(user.getFirstName()); 
                    userRepository.update(user);
                });
    }
}
