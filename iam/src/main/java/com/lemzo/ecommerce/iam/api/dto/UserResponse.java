package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.iam.domain.User;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Réponse détaillée pour un utilisateur.
 */
public record UserResponse(
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    Set<String> roles,
    List<Address> addresses,
    List<Map<String, Object>> paymentMethods
) {
    public static UserResponse from(final User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
            List.copyOf(user.getAddresses()),
            List.copyOf(user.getPaymentMethods())
        );
    }
}
