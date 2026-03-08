package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.iam.domain.User;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Représentation publique d'un utilisateur")
public record UserResponse(
    @Schema(description = "Identifiant unique (UUID v7)")
    UUID id,
    
    @Schema(description = "Nom d'utilisateur", example = "jean.dupont")
    String username,
    
    @Schema(description = "Adresse email", example = "jean@example.com")
    String email,

    @Schema(description = "Prénom")
    String firstName,

    @Schema(description = "Nom")
    String lastName,

    @Schema(description = "Liste des moyens de paiement")
    List<Map<String, Object>> paymentMethods,

    @Schema(description = "Liste des adresses")
    List<Address> addresses,
    
    @Schema(description = "Statut du compte (activé ou non)")
    boolean enabled
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPaymentMethods(),
            user.getAddresses(),
            user.isEnabled()
        );
    }
}
