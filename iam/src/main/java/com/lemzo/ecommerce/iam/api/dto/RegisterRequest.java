package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Requête d'inscription.
 */
@Schema(description = "Données pour l'inscription d'un nouvel utilisateur")
public record RegisterRequest(
    @Schema(description = "Nom d'utilisateur unique", example = "jdoe", required = true)
    @NotBlank(message = "Le nom d'utilisateur est requis")
    String username,
    
    @Schema(description = "Adresse email unique", example = "john.doe@example.com", required = true)
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    String email,
    
    @Schema(description = "Mot de passe (min 8 caractères)", example = "Password123!", required = true)
    @Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    @NotBlank(message = "Le mot de passe est requis")
    String password
) {}
