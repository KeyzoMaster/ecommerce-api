package com.lemzo.ecommerce.iam.api.dto;

/**
 * Requête de mise à jour du profil utilisateur.
 */
public record UserProfileRequest(
    String firstName,
    String lastName
) {}
