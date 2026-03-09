package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.repository.UserRepository;
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

/**
 * Service de gestion des utilisateurs.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Transactional
    public User register(final String username, final String email, final String plainPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessRuleException("error.iam.username_taken");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessRuleException("error.iam.email_taken");
        }

        final String hashedPassword = passwordService.hash(plainPassword.toCharArray());
        final User user = new User(username, email, hashedPassword);
        
        return userRepository.save(user);
    }

    public Optional<User> findById(final UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByIdentifier(final String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier));
    }

    @Transactional
    @Audit(action = "USER_PROFILE_UPDATE")
    public User updateProfile(final UUID userId, final String firstName, final String lastName) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        return userRepository.save(user);
    }

    @Transactional
    @Audit(action = "USER_PAYMENT_METHOD_ADD")
    public User addPaymentMethod(final UUID userId, final String type, final Map<String, Object> details) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        final Map<String, Object> methodDetails = new HashMap<>(details);
        methodDetails.put("type", type);
        user.getPaymentMethods().add(methodDetails);
        
        return userRepository.save(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_ADD")
    public User addAddress(final UUID userId, final Address address) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        if (address.getTechnicalId() == null) {
            address.setTechnicalId(UUID.randomUUID().toString());
        }
        
        user.getAddresses().add(address);
        
        return userRepository.save(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_REMOVE")
    public User removeAddress(final UUID userId, final String addressId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.getAddresses().removeIf(addr -> addr.getTechnicalId().equals(addressId));
        
        return userRepository.save(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_UPDATE")
    public User updateAddress(final UUID userId, final String addressId, final Address updatedAddress) {
        final User user = userRepository.findById(userId)
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

        return userRepository.save(user);
    }
}
