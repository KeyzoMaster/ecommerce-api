package com.lemzo.ecommerce.iam.api;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.iam.api.dto.StoreCreateRequest;
import com.lemzo.ecommerce.iam.api.dto.StoreResponse;
import com.lemzo.ecommerce.iam.service.StoreService;
import com.lemzo.ecommerce.iam.service.UserService;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;

/**
 * Ressource pour la gestion des boutiques.
 */
@Path("/iam/stores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Boutiques", description = "Gestion des boutiques pour les propriétaires")
@SecurityRequirement(name = "jwt")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class StoreResource {

    private final StoreRepository storeRepository;
    private final StoreService storeService;
    private final UserService userService;
    private final HateoasMapper hateoasMapper;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @POST
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.CREATE)
    @Operation(summary = "Ouvrir une boutique", description = "Crée une nouvelle boutique")
    @APIResponse(responseCode = "201", description = "Boutique créée")
    public Response create(@Valid final StoreCreateRequest request) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var owner = userService.findById(principal.getUserId())
                .orElseThrow(() -> new ForbiddenException("Utilisateur non trouvé"));

        final var store = storeService.createStore(request.name(), request.slug(), owner);
        
        return Response.status(Response.Status.CREATED)
                .entity(hateoasMapper.toResource(StoreResponse.from(store), uriInfo))
                .build();
    }

    @GET
    @Path("/{id}")
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.READ)
    @Operation(summary = "Détails d'une boutique", description = "Récupère les informations d'une boutique par son ID")
    public Response getById(@PathParam("id") final UUID id) {
        return storeRepository.findById(id)
                .map(StoreResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PATCH
    @Path("/{id}")
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.UPDATE, checkOwnership = true)
    @Operation(summary = "Modifier une boutique", description = "Met à jour les informations.")
    public Response update(@PathParam("id") final UUID id, @Valid final StoreCreateRequest request) {
        return storeRepository.findById(id)
                .map(store -> {
                    store.setName(request.name());
                    store.setDescription(request.description());
                    final var saved = storeRepository.update(store);
                    return Response.ok(hateoasMapper.toResource(StoreResponse.from(saved), uriInfo)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
