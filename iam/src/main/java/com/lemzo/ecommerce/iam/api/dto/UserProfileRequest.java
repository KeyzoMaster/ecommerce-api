package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de mise à jour du profil utilisateur")
public record UserProfileRequest(
    @Size(max = 50) @Schema(description = "Prénom", example = "Jean")
    String firstName,
    
    @Size(max = 50) @Schema(description = "Nom", example = "Dupont")
    String lastName
) {}
