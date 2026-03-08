package com.lemzo.ecommerce.domain.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Requête pour la création d'une catégorie")
public record CategoryCreateRequest(
    @NotBlank
    @Schema(description = "Nom de la catégorie", example = "Électronique")
    String name,
    
    @NotBlank
    @Schema(description = "Slug unique", example = "electronique")
    String slug,
    
    @Schema(description = "Description")
    String description,
    
    @Schema(description = "ID de la catégorie parente")
    UUID parentId
) {}
