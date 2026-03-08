package com.lemzo.ecommerce.iam.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Requête pour la création d'une boutique")
public record StoreCreateRequest(
    @NotBlank @Schema(description = "Nom de la boutique", example = "Lemzo Tech")
    String name,
    
    @NotBlank @Schema(description = "Slug unique pour l'URL", example = "lemzo-tech")
    String slug,
    
    @Schema(description = "Description de la boutique")
    String description
) {}
