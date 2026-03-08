package com.lemzo.ecommerce.security.infrastructure.ratelimit;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Filtre JAX-RS pour appliquer le Rate Limiting par IP.
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 10) // S'exécute avant l'authentification
public class RateLimitFilter implements ContainerRequestFilter {

    @Inject
    private RateLimitService rateLimitService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Extraction de l'IP (X-Forwarded-For si derrière proxy)
        String ip = requestContext.getHeaderString("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = "anonymous"; // Fallback ou extraire l'IP réelle du HttpServletRequest si nécessaire
        }

        if (!rateLimitService.isAllowed(ip, 100)) { // 100 requêtes par minute
            requestContext.abortWith(Response.status(429) // Too Many Requests
                    .entity("Rate limit exceeded. Try again later.")
                    .build());
        }
    }
}
