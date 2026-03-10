package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.iam.domain.Role;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse pour un rôle.
 */
@Schema(description = "Informations sur un rôle utilisateur")
public record RoleResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Nom du rôle", example = "STORE_OWNER")
    String name,
    
    @Schema(description = "Description du rôle")
    String description,
    
    @Schema(description = "Liste des slugs de permissions associés", example = "[\"product:create\", \"product:update\"]")
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
