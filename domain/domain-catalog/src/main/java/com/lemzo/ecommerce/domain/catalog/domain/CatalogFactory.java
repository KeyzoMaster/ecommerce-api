package com.lemzo.ecommerce.domain.catalog.domain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Factory pour les entités du catalogue.
 */
public final class CatalogFactory {

    private CatalogFactory() {
        // Classe utilitaire
    }

    /**
     * Crée un nouveau produit avec ses attributs par défaut.
     */
    public static Product createProduct(final String name, final String slug, final String sku, 
                                        final BigDecimal price, final Category category) {
        final Product product = new Product(name, slug, sku, price, category);
        product.setActive(true);
        product.setAttributes(new HashMap<>());
        product.setShippingConfig(new HashMap<>());
        product.setWeight(BigDecimal.ZERO);
        product.setViewCount(0);
        return product;
    }

    /**
     * Crée une catégorie.
     */
    public static Category createCategory(final String name, final String slug, final String description) {
        return new Category(name, slug, description);
    }
}
