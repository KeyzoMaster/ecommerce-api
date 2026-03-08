package com.lemzo.ecommerce.api.infrastructure.seeding;

import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.service.CatalogService;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Seeder global pour le catalogue (api module).
 */
@ApplicationScoped
public class CatalogSeeder implements DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(CatalogSeeder.class.getName());

    @Inject
    private CatalogService catalogService;

    @Inject
    private UserService userService;

    @Override
    @Transactional
    public void seed() {
        if (!catalogService.getAllCategories().isEmpty()) {
            LOGGER.info("Catalogue déjà peuplé, saut du seeding.");
            return;
        }

        LOGGER.info("Seeding catalogue...");

        // 1. Création des catégories
        Category electronics = catalogService.createCategory("Électronique", "electronics", "Produits high-tech", null);
        Category smartphones = catalogService.createCategory("Smartphones", "smartphones", "Téléphones mobiles", electronics.getId());
        Category fashion = catalogService.createCategory("Mode", "fashion", "Vêtements et accessoires", null);

        // 2. Récupération du store owner (créé par IamSeeder)
        UUID ownerId = userService.findByIdentifier("owner")
                .map(User::getId)
                .orElseGet(() -> UUID.fromString("00000000-0000-0000-0000-000000000000"));

        // 3. Création des produits
        catalogService.createProduct(
                ownerId,
                "iPhone 15 Pro",
                "iphone-15-pro",
                "IPH15P",
                new BigDecimal("850000"),
                smartphones.getId(),
                Map.of("color", "Titanium", "storage", "256GB"),
                null,
                new BigDecimal("0.2"),
                Map.of("allowed_methods", Map.of("STANDARD", true, "EXPRESS", true))
        );

        catalogService.createProduct(
                ownerId,
                "MacBook Air M3",
                "macbook-air-m3",
                "MBA-M3",
                new BigDecimal("1200000"),
                electronics.getId(),
                Map.of("ram", "16GB", "cpu", "M3"),
                null,
                new BigDecimal("1.2"),
                Map.of("allowed_methods", Map.of("EXPRESS", true))
        );

        catalogService.createProduct(
                ownerId,
                "T-Shirt Coton Bio",
                "tshirt-coton-bio",
                "TS-BIO-01",
                new BigDecimal("15000"),
                fashion.getId(),
                Map.of("material", "Coton Bio", "size", "L"),
                null,
                new BigDecimal("0.15"),
                Map.of()
        );

        LOGGER.info("Catalogue seedé avec succès.");
    }

    @Override
    public int priority() {
        return 2; // Après les utilisateurs
    }
}
