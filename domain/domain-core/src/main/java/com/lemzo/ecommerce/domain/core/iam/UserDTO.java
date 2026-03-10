package com.lemzo.ecommerce.domain.core.iam;

import java.util.UUID;

/**
 * DTO pour les informations utilisateur partagées entre les modules.
 */
public record UserDTO(
    UUID id,
    String username,
    String email
) {}
