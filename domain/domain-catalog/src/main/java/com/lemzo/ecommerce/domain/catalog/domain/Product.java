package com.lemzo.ecommerce.domain.catalog.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.lemzo.ecommerce.core.entity.converter.JsonbConverter;
import jakarta.persistence.Convert;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entité représentant un produit.
 * Utilise JSONB pour les attributs dynamiques (PostgreSQL 18).
 */
@Entity
@Table(name = "catalog_products")
@Getter
@Setter
@NoArgsConstructor
public class Product extends AbstractEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, unique = true)
    private String sku;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency = "XOF";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "view_count")
    private long viewCount = 0;

    @Column(name = "weight")
    private BigDecimal weight = BigDecimal.ZERO;

    /**
     * Configuration spécifique de livraison du vendeur pour ce produit.
     * Exemple : {"allowed_methods": ["EXPRESS"], "free_over": 50000}
     */
    @Convert(converter = com.lemzo.ecommerce.core.entity.converter.JsonbConverter.class)
    @Column(name = "shipping_config", columnDefinition = "text")
    private Map<String, Object> shippingConfig = new HashMap<>();

    /**
     * Vecteur de recherche généré par PostgreSQL (Lecture seule).
     */
    @Column(name = "search_vector", insertable = false, updatable = false)
    private String searchVector;

    /**
     * Attributs spécifiques au produit (ex: taille, couleur, specs techniques).
     * Stocké en JSONB dans PostgreSQL via un convertisseur Jakarta Persistence.
     */
    @Convert(converter = JsonbConverter.class)
    @Column(columnDefinition = "text")
    private Map<String, Object> attributes;

    public Product(String name, String slug, String sku, BigDecimal price, Category category) {
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.price = price;
        this.category = category;
    }
}
