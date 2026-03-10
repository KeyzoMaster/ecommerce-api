package com.lemzo.ecommerce.domain.core.iam;

import com.lemzo.ecommerce.core.domain.Address;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

/**
 * Port agrégé pour les opérations liées aux utilisateurs et à l'accès.
 */
public interface UserPort {
    Optional<? extends Object> findById(UUID userId);

    Optional<UserDTO> findUserById(UUID id);

    Optional<? extends Object> findByIdentifier(String identifier);
    Optional<Address> findAddressById(UUID userId, String addressId);
    Set<String> getPermissions(UUID userId);
    boolean canAccessStore(UUID userId, UUID storeId);
}
