package com.lemzo.ecommerce.iam.api;

import com.lemzo.ecommerce.core.api.dto.Link;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.iam.api.dto.AuthResponse;
import com.lemzo.ecommerce.iam.api.dto.LoginRequest;
import com.lemzo.ecommerce.iam.api.dto.RegisterRequest;
import com.lemzo.ecommerce.iam.api.dto.UserResponse;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.service.AuthenticationService;
import com.lemzo.ecommerce.iam.service.UserService;
import com.lemzo.ecommerce.security.infrastructure.jwt.JwtService;
import com.lemzo.ecommerce.security.infrastructure.jwt.TokenRevocationService;
import io.jsonwebtoken.Claims;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;

/**
 * Ressource JAX-RS pour l'authentification et l'inscription.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentification", description = "Gestion de l'accès utilisateur et des inscriptions")
public class AuthResource {

    @Inject
    private AuthenticationService authService;

    @Inject
    private UserService userService;

    @Inject
    private TokenRevocationService tokenRevocationService;

    @Inject
    private JwtService jwtService;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/register")
    @Operation(summary = "Inscrire un nouvel utilisateur", description = "Crée un compte utilisateur standard dans le système")
    @APIResponse(responseCode = "201", description = "Utilisateur créé avec succès")
    public Response register(@Valid RegisterRequest request) {
        User user = userService.createUser(request.username(), request.email(), request.password());
        var response = hateoasMapper.toResource(UserResponse.from(user), uriInfo);
        
        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @POST
    @Path("/login")
    @Operation(summary = "Se connecter", description = "Authentifie un utilisateur et retourne les jetons JWT")
    @APIResponse(responseCode = "200", description = "Authentification réussie")
    public Response login(@Valid LoginRequest request) {
        AuthenticationService.LoginResult result = authService.login(request.identifier(), request.password());

        var links = List.of(
            Link.self(uriInfo.getAbsolutePath().toString()),
            Link.of("user-profile", uriInfo.getBaseUriBuilder().path("/users/me").build().toString(), "GET")
        );

        var response = new AuthResponse(result.accessToken(), result.refreshToken(), links);
        return Response.ok(response).build();
    }

    @POST
    @Path("/logout")
    @Operation(summary = "Se déconnecter", description = "Invalide le jeton d'accès actuel")
    @APIResponse(responseCode = "204", description = "Déconnexion réussie")
    public Response logout(@HeaderParam("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtService.validateToken(token);
                String jti = claims.getId();
                if (jti != null && claims.getExpiration() != null) {
                    long exp = claims.getExpiration().getTime();
                    long now = System.currentTimeMillis();
                    long ttl = (exp - now) / 1000;
                    if (ttl > 0) {
                        tokenRevocationService.revoke(jti, ttl);
                    }
                }
            } catch (Exception e) {
                // Token déjà invalide ou malformé
            }
        }
        return Response.noContent().build();
    }
}
