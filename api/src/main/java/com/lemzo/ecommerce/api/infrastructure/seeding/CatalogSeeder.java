package com.lemzo.ecommerce.api.infrastructure.seeding;

import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.repository.CategoryRepository;
import com.lemzo.ecommerce.domain.catalog.service.CatalogService;
import com.lemzo.ecommerce.domain.marketing.repository.CouponRepository;
import com.lemzo.ecommerce.domain.marketing.domain.MarketingFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Seeder pour le catalogue produits et catégories.
 */
@ApplicationScoped
public class CatalogSeeder implements DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(CatalogSeeder.class.getName());

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private CatalogService catalogService;

    @Inject
    private CouponRepository couponRepository;

    @Override
    @Transactional
    public void seed() {
        if (!categoryRepository.findAll().isEmpty()) {
            LOGGER.info("Catalog already seeded. Skipping.");
            return;
        }

        LOGGER.info("Seeding Catalog data...");

        // 1. Catégories
        final Category electronics = catalogService.createCategory("Électronique", "electronics", "Gadgets et appareils", null);
        final Category computers = catalogService.createCategory("Ordinateurs", "computers", "Laptops et desktops", electronics.getId());
        final Category smartphones = catalogService.createCategory("Smartphones", "smartphones", "Téléphones mobiles", electronics.getId());

        // 2. Produits
        final UUID storeId = UUID.randomUUID();
        
        catalogService.createProduct("MacBook Pro M3", "macbook-pro-m3", "AAPL-MBP-M3", 
                new BigDecimal("1500000"), computers.getId(), Map.of(), null, BigDecimal.ZERO, Map.of());
        
        catalogService.createProduct("iPhone 15 Pro", "iphone-15-pro", "AAPL-I15P", 
                new BigDecimal("850000"), smartphones.getId(), Map.of(), null, BigDecimal.ZERO, Map.of());
        
        catalogService.createProduct("Dell XPS 15", "dell-xps-15", "DELL-XPS15", 
                new BigDecimal("1200000"), computers.getId(), Map.of(), null, BigDecimal.ZERO, Map.of());

        // 3. Coupons
        couponRepository.insert(MarketingFactory.createCoupon("WELCOME10", "PERCENTAGE", new BigDecimal("10")));
        couponRepository.insert(MarketingFactory.createCoupon("REDUC5000", "FIXED_AMOUNT", new BigDecimal("5000")));
        couponRepository.insert(MarketingFactory.createCoupon("BLACKFRIDAY", "PERCENTAGE", new BigDecimal("30")));

        LOGGER.info("Catalog seeding completed.");
    }

    @Override
    public int priority() {
        return 2;
    }
}
