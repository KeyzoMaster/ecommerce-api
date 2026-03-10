package com.lemzo.ecommerce.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Set;
import java.util.HashSet;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import org.eclipse.microprofile.openapi.annotations.servers.Server;

/**
 * Configuration JAX-RS et Documentation OpenAPI.
 * La DataSource est définie dans WEB-INF/glassfish-resources.xml.
 */
@ApplicationPath("/v1")
@OpenAPIDefinition(
    info = @Info(
        title = "E-Commerce API",
        version = "1.0.0",
        description = "API professionnelle pour une plateforme multi-boutique (Modular Monolith). " +
                      "Gère le cycle de vie complet : IAM, Catalogue, Ventes, Stocks et Expéditions.",
        contact = @Contact(name = "Support Technique", email = "support@lemzo.com"),
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")
    ),
    servers = {
        @Server(url = "/api", description = "Serveur de développement Local")
    },
    tags = {
        @Tag(name = "Authentification", description = "Accès et sécurité"),
        @Tag(name = "Utilisateurs", description = "Profils et adresses"),
        @Tag(name = "Catalogue : Produits", description = "Gestion du catalogue"),
        @Tag(name = "Ventes : Commandes", description = "Tunnel d'achat"),
        @Tag(name = "Analyses", description = "Reporting et statistiques")
    },
    security = @SecurityRequirement(name = "jwt")
)
@SecuritySchemes({
    @SecurityScheme(
        securitySchemeName = "jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Authentification basée sur les jetons JWT. " +
                      "Ajoutez 'Bearer {token}' dans le header Authorization."
    )
})
public class HelloApplication extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();
        
        // Swagger Resource
        classes.add(OpenApiResource.class);
        
        // Modules IAM
        classes.add(com.lemzo.ecommerce.iam.api.AuthResource.class);
        classes.add(com.lemzo.ecommerce.iam.api.UserResource.class);
        classes.add(com.lemzo.ecommerce.iam.api.StoreResource.class);
        classes.add(com.lemzo.ecommerce.iam.api.RoleResource.class);
        
        // Modules Catalog
        classes.add(com.lemzo.ecommerce.domain.catalog.api.ProductResource.class);
        classes.add(com.lemzo.ecommerce.domain.catalog.api.CategoryResource.class);
        
        // Modules Sales
        classes.add(com.lemzo.ecommerce.domain.sales.api.OrderResource.class);
        classes.add(com.lemzo.ecommerce.domain.sales.api.CartResource.class);
        
        // Modules Analytics
        classes.add(com.lemzo.ecommerce.domain.analytics.api.AnalyticsResource.class);
        
        // Modules Inventory & Shipping
        classes.add(com.lemzo.ecommerce.domain.inventory.api.InventoryResource.class);
        classes.add(com.lemzo.ecommerce.domain.shipping.api.ShippingResource.class);
        
        // Modules Marketing
        classes.add(com.lemzo.ecommerce.domain.marketing.api.CouponResource.class);
        
        // Core & Infrastructure
        classes.add(com.lemzo.ecommerce.api.HelloResource.class);
        classes.add(com.lemzo.ecommerce.api.infrastructure.web.SeedingResource.class);
        
        // Providers (Mappers, Filters)
        classes.add(com.lemzo.ecommerce.core.api.exception.GlobalExceptionMapper.class);
        classes.add(com.lemzo.ecommerce.security.infrastructure.jwt.JwtAuthenticationFilter.class);
        classes.add(com.lemzo.ecommerce.security.infrastructure.ratelimit.RateLimitFilter.class);
        
        return classes;
    }

    public HelloApplication() {
        super();
    }
}
