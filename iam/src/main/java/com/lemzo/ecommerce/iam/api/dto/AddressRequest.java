package com.lemzo.ecommerce.iam.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import com.lemzo.ecommerce.core.domain.Address;

/**
 * Requête de création ou mise à jour d'adresse.
 */
@Schema(description = "Données d'une adresse postale")
public record AddressRequest(
    @Schema(description = "Libellé de l'adresse", example = "Maison", required = true)
    String label,
    
    @Schema(description = "Rue et numéro", example = "123 Rue de la République", required = true)
    String street,
    
    @Schema(description = "Ville", example = "Dakar", required = true)
    String city,
    
    @Schema(description = "Code postal", example = "12500")
    String zipCode,
    
    @Schema(description = "Pays", example = "Sénégal", required = true)
    String country
) {
    public Address toEntity() {
        return Address.builder()
                .label(label)
                .street(street)
                .city(city)
                .zipCode(zipCode)
                .country(country)
                .build();
    }
}
