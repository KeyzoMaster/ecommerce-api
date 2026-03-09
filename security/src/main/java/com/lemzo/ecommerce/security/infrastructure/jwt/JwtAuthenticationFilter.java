package com.lemzo.ecommerce.security.infrastructure.jwt;

import com.lemzo.ecommerce.security.infrastructure.pabc.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        Optional.ofNullable(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .ifPresent(token -> processToken(requestContext, token));
    }

    private void processToken(final ContainerRequestContext requestContext, final String token) {
        try {
            final Claims claims = jwtService.validateToken(token);
            
            if (tokenRevocationService.isRevoked(claims.getId())) {
                requestContext.abortWith(Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity("Token has been revoked.")
                        .build());
                return;
            }

            final UUID userId = UUID.fromString(claims.getSubject());
            final String email = claims.get("email", String.class);
            
            @SuppressWarnings("unchecked")
            final List<String> permissionsFromClaims = (List<String>) claims.get("permissions", List.class);
            final Set<String> permissions = new HashSet<>(Optional.ofNullable(permissionsFromClaims).orElse(List.of()));

            final UserPrincipal userPrincipal = new UserPrincipal(userId, email, permissions);
            final SecurityContext originalContext = requestContext.getSecurityContext();

            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() { return userPrincipal; }
                @Override
                public boolean isUserInRole(final String role) { return permissions.contains(role); }
                @Override
                public boolean isSecure() { return originalContext.isSecure(); }
                @Override
                public String getAuthenticationScheme() { return "Bearer"; }
            });

        } catch (final Exception _) {
            // Variable anonyme car l'exception est ignorée à dessein pour les ressources publiques
        }
    }
}
