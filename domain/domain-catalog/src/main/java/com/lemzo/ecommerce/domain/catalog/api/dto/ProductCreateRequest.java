package com.lemzo.ecommerce.domain.catalog.api.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Requête de création de produit.
 */
@Schema(description = "Données pour la création d'un produit")
public record ProductCreateRequest(
    @Schema(description = "Nom du produit", example = "MacBook Air M2", required = true)
    @NotBlank(message = "Le nom est requis")
    String name,
    
    @Schema(description = "Slug unique pour l'URL", example = "macbook-air-m2", required = true)
    @NotBlank(message = "Le slug est requis")
    String slug,
    
    @Schema(description = "Référence stock unique", example = "AAPL-MBA-M2", required = true)
    @NotBlank(message = "Le SKU est requis")
    String sku,
    
    @Schema(description = "Description du produit")
    String description,
    
    @Schema(description = "Prix unitaire", example = "750000", required = true)
    @NotNull(message = "Le prix est requis")
    @Positive(message = "Le prix doit être positif")
    BigDecimal price,
    
    @Schema(description = "ID de la catégorie parente", required = true)
    @NotNull(message = "La catégorie est requise")
    UUID categoryId,
    
    @Schema(description = "Attributs dynamiques (taille, couleur, etc.)")
    Map<String, Object> attributes,
    
    @Schema(description = "URL de l'image initiale")
    String imageUrl,
    
    @Schema(description = "Poids du produit en kg", example = "1.24")
    BigDecimal weight,
    
    @Schema(description = "Configuration logistique (JSON)")
    Map<String, Object> shippingConfig
) {}
