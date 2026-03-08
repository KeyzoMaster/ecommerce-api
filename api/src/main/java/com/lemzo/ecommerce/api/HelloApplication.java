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

@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "E-Commerce API",
        version = "1.0.0",
        description = "API professionnelle pour une plateforme multi-boutique (Modular Monolith)",
        contact = @Contact(name = "Support Technique", email = "support@lemzo.com"),
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")
    ),
    security = @SecurityRequirement(name = "jwt")
)
@SecuritySchemes({
    @SecurityScheme(
        securitySchemeName = "jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Authentification basée sur les jetons JWT"
    )
})
public class HelloApplication extends Application {
}
