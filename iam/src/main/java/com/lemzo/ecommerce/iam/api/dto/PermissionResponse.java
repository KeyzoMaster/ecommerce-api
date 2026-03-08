package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.iam.domain.Permission;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Détails d'une permission")
public record PermissionResponse(
    @Schema(description = "Identifiant unique")
    UUID id,
    
    @Schema(description = "Slug de la permission", example = "catalog:create")
    String slug
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getSlug());
    }
}
