package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.iam.domain.Role;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Réponse pour un rôle.
 */
public record RoleResponse(
    UUID id,
    String name,
    String description,
    Set<String> permissions
) {
    public static RoleResponse from(final Role role) {
        return new RoleResponse(
            role.getId(),
            role.getName(),
            role.getDescription(),
            role.getPermissions().stream()
                .map(p -> p.getSlug())
                .collect(Collectors.toSet())
        );
    }
}
