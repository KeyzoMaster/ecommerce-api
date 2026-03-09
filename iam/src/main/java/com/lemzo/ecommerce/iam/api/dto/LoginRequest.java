package com.lemzo.ecommerce.iam.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de connexion.
 */
public record LoginRequest(
    @NotBlank(message = "L'identifiant est requis")
    String identifier,
    
    @NotBlank(message = "Le mot de passe est requis")
    String password
) {}
