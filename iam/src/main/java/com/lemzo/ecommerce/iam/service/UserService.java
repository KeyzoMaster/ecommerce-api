package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.repository.UserRepository;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des utilisateurs.
 */
@ApplicationScoped
public class UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private PasswordService passwordService;

    /**
     * Crée un nouvel utilisateur.
     */
    @Transactional
    @Audit(action = "USER_CREATE")
    public User createUser(String username, String email, String plainPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("error.iam.email_already_exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessRuleException("error.iam.username_already_exists");
        }

        String hashedPassword = passwordService.hash(plainPassword.toCharArray());
        User user = new User(username, email, hashedPassword);
        
        return userRepository.insert(user);
    }

    /**
     * Récupère un utilisateur par son identifiant.
     */
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Récupère un utilisateur par son email ou username.
     */
    public Optional<User> findByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier));
    }

    @Transactional
    @Audit(action = "USER_PROFILE_UPDATE")
    public User updateProfile(UUID userId, String firstName, String lastName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        return userRepository.save(user);
    }

    @Transactional
    @Audit(action = "USER_PAYMENT_METHOD_ADD")
    public User addPaymentMethod(UUID userId, String type, Map<String, Object> details) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        details.put("type", type);
        user.getPaymentMethods().add(details);
        
        return userRepository.save(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_ADD")
    public User addAddress(UUID userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        // S'assurer qu'un ID est généré si non fourni
        if (address.getId() == null) {
            address.setId(java.util.UUID.randomUUID().toString());
        }
        
        user.getAddresses().add(address);
        
        return userRepository.save(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_REMOVE")
    public User removeAddress(UUID userId, String addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));
        
        user.getAddresses().removeIf(addr -> addr.getId().equals(addressId));
        
        return userRepository.save(user);
    }

    @Transactional
    @Audit(action = "USER_ADDRESS_UPDATE")
    public User updateAddress(UUID userId, String addressId, Address updatedAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));

        user.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
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
