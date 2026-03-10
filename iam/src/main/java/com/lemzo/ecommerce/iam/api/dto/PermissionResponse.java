package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.iam.domain.Permission;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Réponse pour une permission.
 */
@Schema(description = "Informations sur une permission du système")
public record PermissionResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Slug de la permission (ressource:action)", example = "product:create")
    String slug
) {
    public static PermissionResponse from(final Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getSlug());
    }
}
