package com.lemzo.ecommerce.domain.catalog.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Requête pour la mise à jour partielle d'un produit")
public record ProductUpdateRequest(
    @Schema(description = "Nouveau nom du produit")
    String name,
    
    @Schema(description = "Nouveau slug")
    String slug,
    
    @Schema(description = "Nouveau SKU")
    String sku,

    @Schema(description = "Nouvelle description")
    String description,
    
    @Schema(description = "Nouveau prix")
    BigDecimal price,
    
    @Schema(description = "Nouvelle catégorie")
    UUID categoryId,

    @Schema(description = "Statut actif")
    Boolean active,
    
    @Schema(description = "Nouveaux attributs")
    Map<String, Object> attributes,

    @Schema(description = "Nouvelle URL d'image")
    String imageUrl,

    @Schema(description = "Nouveau poids (kg)")
    BigDecimal weight,

    @Schema(description = "Nouvelle configuration livraison (JSON)")
    Map<String, Object> shippingConfig
) {}
