package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.iam.domain.Role;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "Représentation d'un rôle utilisateur")
public record RoleResponse(
    UUID id,
    String name,
    String description,
    boolean systemRole,
    List<PermissionResponse> permissions
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
            role.getId(),
            role.getName(),
            role.getDescription(),
            role.isSystemRole(),
            role.getPermissions().stream()
                .map(PermissionResponse::from)
                .collect(Collectors.toList())
        );
    }
}
