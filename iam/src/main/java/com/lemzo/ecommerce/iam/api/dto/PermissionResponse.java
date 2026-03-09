package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.iam.domain.Permission;
import java.util.UUID;

/**
 * Réponse pour une permission.
 */
public record PermissionResponse(
    UUID id,
    String slug
) {
    public static PermissionResponse from(final Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getSlug());
    }
}
