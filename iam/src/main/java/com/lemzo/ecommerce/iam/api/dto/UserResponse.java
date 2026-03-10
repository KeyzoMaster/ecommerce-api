package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.iam.domain.User;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse détaillée pour un utilisateur.
 */
@Schema(description = "Informations détaillées du profil utilisateur")
public record UserResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Nom d'utilisateur", example = "jdoe")
    String username,
    
    @Schema(description = "Adresse email", example = "john.doe@example.com")
    String email,
    
    @Schema(description = "Prénom", example = "John")
    String firstName,
    
    @Schema(description = "Nom de famille", example = "Doe")
    String lastName,
    
    @Schema(description = "Liste des noms de rôles")
    Set<String> roles,
    
    @Schema(description = "Liste des adresses enregistrées")
    List<Address> addresses,
    
    @Schema(description = "Moyens de paiement (Mocks)")
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
