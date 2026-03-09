package com.lemzo.ecommerce.iam.api.dto;

import java.util.Set;

/**
 * Requête de création de rôle.
 */
public record RoleCreateRequest(
    String name,
    String description,
    Set<String> permissionSlugs
) {}
