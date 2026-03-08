package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête d'ajout ou modification d'adresse")
public record AddressRequest(
    @NotBlank @Schema(description = "Libellé de l'adresse (ex: Maison, Bureau)", example = "Maison")
    String label,

    @NotBlank @Schema(description = "Rue / Avenue", example = "Place de l'Indépendance")
    String street,
    
    @NotBlank @Schema(description = "Ville", example = "Dakar")
    String city,
    
    @Schema(description = "Code postal", example = "10000")
    String zipCode,
    
    @NotBlank @Schema(description = "Pays", example = "Sénégal")
    String country
) {}
