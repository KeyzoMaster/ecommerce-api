package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête de connexion.
 */
@Schema(description = "Données pour l'authentification")
public record LoginRequest(
    @Schema(description = "Nom d'utilisateur ou adresse email", example = "jdoe", required = true)
    @NotBlank(message = "L'identifiant est requis")
    String identifier,
    
    @Schema(description = "Mot de passe de l'utilisateur", example = "Password123!", required = true)
    @NotBlank(message = "Le mot de passe est requis")
    String password
) {}
