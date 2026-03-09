package com.lemzo.ecommerce.api.infrastructure.web;

import com.lemzo.ecommerce.api.infrastructure.seeding.DatabaseSeeder;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import java.util.Map;

/**
 * Endpoint manuel pour déclencher le seeding de la base de données.
 * Uniquement disponible en mode développement.
 */
@Path("/dev/seed")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class SeedingResource {

    @Inject
    private DatabaseSeeder databaseSeeder;

    @POST
    public Response seed() {
        final String profile = ConfigProvider.getConfig()
                .getOptionalValue("mp.config.profile", String.class)
                .orElse("dev");

        if (!"dev".equalsIgnoreCase(profile)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Seeding only allowed in dev profile"))
                    .build();
        }

        final boolean success = databaseSeeder.run();

        if (success) {
            return Response.ok(Map.of("message", "Seeding executed successfully")).build();
        } else {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("message", "Seeding already executed or not allowed"))
                    .build();
        }
    }
}
