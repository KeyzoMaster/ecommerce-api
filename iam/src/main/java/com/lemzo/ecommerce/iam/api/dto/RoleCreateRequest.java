package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.Set;

/**
 * Requête de création de rôle.
 */
@Schema(description = "Données pour la création d'un nouveau rôle")
public record RoleCreateRequest(
    @Schema(description = "Nom du rôle", example = "MANAGER", required = true)
    String name,
    
    @Schema(description = "Description du rôle")
    String description,
    
    @Schema(description = "Liste des slugs de permissions à associer", example = "[\"order:read\", \"order:manage\"]")
    Set<String> permissionSlugs
) {}
