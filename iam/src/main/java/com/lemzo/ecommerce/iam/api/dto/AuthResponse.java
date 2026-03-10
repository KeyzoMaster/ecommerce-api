package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Résultat d'une authentification réussie")
public record AuthResponse(
    @Schema(description = "Jeton d'accès JWT")
    String accessToken,
    
    @Schema(description = "Jeton de rafraîchissement")
    String refreshToken,
    
    @Schema(description = "Adresse email de l'utilisateur", example = "john.doe@example.com")
    String email,
    
    @Schema(description = "Liste des slugs de permissions accordées", example = "[\"product:read\", \"order:create\"]")
    Set<String> permissions
) {}
