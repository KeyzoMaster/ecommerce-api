package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.core.domain.Address;

/**
 * Requête de création ou mise à jour d'adresse.
 */
public record AddressRequest(
    String label,
    String street,
    String city,
    String zipCode,
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
