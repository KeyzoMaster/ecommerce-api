package com.lemzo.ecommerce.iam.api.dto;

import java.util.Set;

/**
 * Réponse d'authentification contenant le token et les infos utilisateur.
 */
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String email,
    Set<String> permissions
) {}
