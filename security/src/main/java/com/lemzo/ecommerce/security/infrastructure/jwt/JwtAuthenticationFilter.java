package com.lemzo.ecommerce.security.infrastructure.jwt;

import com.lemzo.ecommerce.security.infrastructure.pabc.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Filtre JAX-RS pour l'authentification JWT.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    @Inject
    private JwtService jwtService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Optional.ofNullable(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .ifPresent(token -> {
                    try {
                        Claims claims = jwtService.validateToken(token);
                        UUID userId = UUID.fromString(claims.getSubject());
                        String email = claims.get("email", String.class);
                        
                        @SuppressWarnings("unchecked")
                        Set<String> permissions = new HashSet<>(
                                Optional.ofNullable(claims.get("permissions", List.class))
                                        .orElseGet(List::of)
                        );

                        UserPrincipal principal = new UserPrincipal(userId, email, permissions);

                        SecurityContext originalContext = requestContext.getSecurityContext();
                        requestContext.setSecurityContext(new SecurityContext() {
                            @Override
                            public Principal getUserPrincipal() { return principal; }
                            @Override
                            public boolean isUserInRole(String role) { return permissions.contains(role); }
                            @Override
                            public boolean isSecure() { return originalContext.isSecure(); }
                            @Override
                            public String getAuthenticationScheme() { return "Bearer"; }
                        });

                    } catch (Exception e) {
                        // Token invalide ou expiré
                    }
                });
    }
}
