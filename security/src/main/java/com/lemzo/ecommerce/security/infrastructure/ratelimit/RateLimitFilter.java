package com.lemzo.ecommerce.security.infrastructure.ratelimit;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.Optional;

/**
 * Filtre de limitation de débit par IP.
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 10)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RateLimitFilter implements ContainerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String ip = requestContext.getHeaderString("X-Forwarded-For");
        
        final String clientIp = Optional.ofNullable(ip)
                .filter(address -> !address.isBlank())
                .orElse("anonymous");

        if (!rateLimitService.isAllowed(clientIp, 100)) {
            requestContext.abortWith(Response
                    .status(Response.Status.TOO_MANY_REQUESTS)
                    .entity("Rate limit exceeded. Try again later.")
                    .build());
        }
    }
}
