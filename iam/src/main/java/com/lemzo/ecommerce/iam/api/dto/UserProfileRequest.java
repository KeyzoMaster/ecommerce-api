package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Requête de mise à jour du profil utilisateur.
 */
@Schema(description = "Données pour la mise à jour du profil")
public record UserProfileRequest(
    @Schema(description = "Prénom", example = "John")
    String firstName,
    
    @Schema(description = "Nom de famille", example = "Doe")
    String lastName
) {}
