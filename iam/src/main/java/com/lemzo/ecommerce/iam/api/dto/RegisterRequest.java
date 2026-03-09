package com.lemzo.ecommerce.iam.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Requête d'inscription.
 */
public record RegisterRequest(
    @NotBlank(message = "Le nom d'utilisateur est requis")
    String username,
    
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    String email,
    
    @Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    @NotBlank(message = "Le mot de passe est requis")
    String password
) {}
