package com.lemzo.ecommerce.domain.catalog.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Requête de mise à jour de produit.
 */
@Schema(description = "Données pour la mise à jour d'un produit")
public record ProductUpdateRequest(
    @Schema(description = "Nouveau nom du produit")
    String name,
    
    @Schema(description = "Nouveau slug")
    String slug,
    
    @Schema(description = "Nouvelle référence stock")
    String sku,
    
    @Schema(description = "Nouvelle description")
    String description,
    
    @Schema(description = "Nouveau prix")
    BigDecimal price,
    
    @Schema(description = "ID de la nouvelle catégorie")
    UUID categoryId,
    
    @Schema(description = "État d'activation")
    Boolean active,
    
    @Schema(description = "Nouveaux attributs dynamiques")
    Map<String, Object> attributes,
    
    @Schema(description = "Nouvelle URL d'image")
    String imageUrl,
    
    @Schema(description = "Nouveau poids en kg")
    BigDecimal weight,
    
    @Schema(description = "Nouvelle configuration logistique")
    Map<String, Object> shippingConfig
) {}
