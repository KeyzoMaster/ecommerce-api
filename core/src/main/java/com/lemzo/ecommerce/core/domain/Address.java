package com.lemzo.ecommerce.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objet de valeur pour représenter une adresse.
 * Partagé entre les modules via le CORE.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    /**
     * Identifiant technique unique pour la gestion CRUD (Module IAM).
     */
    @Builder.Default
    @Column(name = "technical_id")
    private String technicalId = java.util.UUID.randomUUID().toString();

    /**
     * Libellé de l'adresse (ex: Maison, Bureau, Livraison).
     */
    @Column(name = "label")
    private String label;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "country")
    private String country;
}
