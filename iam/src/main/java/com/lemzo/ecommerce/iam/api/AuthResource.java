package com.lemzo.ecommerce.iam.api;

import com.lemzo.ecommerce.core.api.dto.Link;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.iam.api.dto.AuthResponse;
import com.lemzo.ecommerce.iam.api.dto.LoginRequest;
import com.lemzo.ecommerce.iam.api.dto.RegisterRequest;
import com.lemzo.ecommerce.iam.api.dto.UserResponse;
import com.lemzo.ecommerce.iam.service.AuthenticationService;
import com.lemzo.ecommerce.iam.service.UserService;
import com.lemzo.ecommerce.security.infrastructure.jwt.JwtService;
import com.lemzo.ecommerce.security.infrastructure.jwt.TokenRevocationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.Optional;

/**
 * Ressource JAX-RS pour l'authentification et l'inscription.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentification", description = "Gestion de l'accès utilisateur et des inscriptions")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuthResource {

    private final AuthenticationService authService;
    private final UserService userService;
    private final TokenRevocationService tokenRevocationService;
    private final JwtService jwtService;
    private final HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/register")
    @Operation(summary = "Inscrire un nouvel utilisateur", description = "Crée un compte utilisateur standard")
    @APIResponse(responseCode = "201", description = "Utilisateur créé avec succès")
    public Response register(@Valid final RegisterRequest request) {
        final var user = userService.register(request.username(), request.email(), request.password());
        final var response = hateoasMapper.toResource(UserResponse.from(user), uriInfo);
        
        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @POST
    @Path("/login")
    @Operation(summary = "Se connecter", description = "Authentifie un utilisateur et retourne les jetons JWT")
    public Response login(@Valid final LoginRequest request) {
        final var result = authService.login(request.identifier(), request.password());
        return Response.ok(result).build();
    }

    @POST
    @Path("/logout")
    @Operation(summary = "Se déconnecter", description = "Invalide le jeton d'accès actuel")
    public Response logout(@HeaderParam("Authorization") final String authHeader) {
        Optional.ofNullable(authHeader)
                .filter(h -> h.startsWith("Bearer "))
                .map(h -> h.substring(7))
                .ifPresent(this::revokeToken);
        return Response.noContent().build();
    }

    private void revokeToken(final String token) {
        try {
            final var claims = jwtService.validateToken(token);
            Optional.ofNullable(claims.getExpiration())
                    .ifPresent(exp -> {
                        final long ttl = (exp.getTime() - System.currentTimeMillis()) / 1000;
                        if (ttl > 0) {
                            tokenRevocationService.revoke(claims.getId(), ttl);
                        }
                    });
        } catch (final Exception e) {
            // Ignorer si le token est déjà invalide
        }
    }
}
