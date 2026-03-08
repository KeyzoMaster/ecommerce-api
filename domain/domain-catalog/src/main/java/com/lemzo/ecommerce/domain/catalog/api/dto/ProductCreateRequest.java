package com.lemzo.ecommerce.domain.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Requête pour la création d'un produit")
public record ProductCreateRequest(
    @NotBlank 
    @Schema(description = "Nom du produit", example = "iPhone 16 Pro")
    String name,
    
    @NotBlank 
    @Schema(description = "Slug unique", example = "iphone-16-pro")
    String slug,
    
    @NotBlank 
    @Schema(description = "SKU unique", example = "APPLE-IPH16P-256")
    String sku,
    
    @NotNull @Positive 
    @Schema(description = "Prix unitaire", example = "1199.99")
    BigDecimal price,
    
    @NotNull 
    @Schema(description = "ID de la catégorie parente")
    UUID categoryId,
    
    @Schema(description = "Attributs additionnels (JSON)", example = "{\"couleur\": \"Noir\", \"ram\": \"8GB\"}")
    Map<String, Object> attributes,
    
    @Schema(description = "URL ou chemin de l'image principale", example = "products/iphone.jpg")
    String imageUrl,

    @Schema(description = "Poids du produit en kg", example = "0.5")
    BigDecimal weight,

    @Schema(description = "Configuration livraison (JSON)", example = "{\"allowed_methods\": [\"EXPRESS\"]}")
    Map<String, Object> shippingConfig
) {}
