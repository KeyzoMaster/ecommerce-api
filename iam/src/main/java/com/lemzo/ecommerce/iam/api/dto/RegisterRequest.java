package com.lemzo.ecommerce.iam.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Requête pour l'inscription d'un nouvel utilisateur")
public record RegisterRequest(
    @NotBlank(message = "Le nom d'utilisateur est requis")
    @Size(min = 3, max = 50)
    @Schema(description = "Nom d'utilisateur unique", example = "jean.dupont")
    String username,

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse email de l'utilisateur", example = "jean@example.com")
    String email,

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    @Schema(description = "Mot de passe sécurisé", example = "P@ssw0rd2025")
    String password
) {}
