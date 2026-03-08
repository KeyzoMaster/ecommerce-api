package com.lemzo.ecommerce.iam.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Requête pour la création d'un rôle")
public record RoleCreateRequest(
    @NotBlank 
    @Schema(description = "Nom unique du rôle", example = "BOUTIQUE_MANAGER")
    String name,
    
    @Schema(description = "Description fonctionnelle")
    String description,
    
    @Schema(description = "Liste des identifiants de permissions à associer")
    Set<UUID> permissionIds
) {}
