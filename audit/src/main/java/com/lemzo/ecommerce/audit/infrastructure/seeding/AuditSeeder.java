package com.lemzo.ecommerce.audit.infrastructure.seeding;

import com.lemzo.ecommerce.audit.service.AuditService;
import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Seeder pour générer des logs d'audit initiaux dans MongoDB.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuditSeeder implements DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(AuditSeeder.class.getName());
    private final AuditService auditService;

    @Override
    public void seed() {
        LOGGER.info("Seeding Audit logs in MongoDB...");

        final UUID fakeUser = UUID.randomUUID();

        auditService.log(fakeUser, "SYSTEM_STARTUP", ResourceType.PLATFORM, "0", 
                Map.of("version", "1.0-SNAPSHOT"), "127.0.0.1", "GlassFish 8");
        
        auditService.log(fakeUser, "CATALOG_IMPORT", ResourceType.CATALOG, "all", 
                Map.of("items", 5), "127.0.0.1", "Seeder");

        LOGGER.info("Audit seeding completed.");
    }

    @Override
    public int priority() {
        return 4; // En dernier
    }
}
