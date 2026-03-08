package com.lemzo.ecommerce.iam.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Requête pour la connexion d'un utilisateur")
public record LoginRequest(
    @NotBlank(message = "L'identifiant est requis")
    @Schema(description = "Nom d'utilisateur ou email", example = "jean.dupont")
    String identifier,

    @NotBlank(message = "Le mot de passe est requis")
    @Schema(description = "Mot de passe de l'utilisateur", example = "P@ssw0rd2025")
    String password
) {}
