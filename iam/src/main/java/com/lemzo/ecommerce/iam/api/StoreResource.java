package com.lemzo.ecommerce.iam.api;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.iam.api.dto.StoreCreateRequest;
import com.lemzo.ecommerce.iam.api.dto.StoreResponse;
import com.lemzo.ecommerce.iam.domain.Store;
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
public class StoreResource {

    @Inject
    private StoreRepository storeRepository;

    @Inject
    private StoreService storeService;

    @Inject
    private UserService userService;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @POST
    @HasPermission(resource = ResourceType.STORE, action = PbacAction.CREATE)
    @Operation(summary = "Ouvrir une boutique", description = "Crée une nouvelle boutique rattachée à l'utilisateur actuel")
    @APIResponse(responseCode = "201", description = "Boutique créée")
    public Response create(@Valid StoreCreateRequest request) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        var owner = userService.findById(principal.getUserId())
                .orElseThrow(() -> new ForbiddenException("Utilisateur non trouvé"));

        Store store = new Store(request.name(), request.slug(), owner);
        store.setDescription(request.description());
        
        var saved = storeRepository.insert(store);
        return Response.status(Response.Status.CREATED)
                .entity(hateoasMapper.toResource(StoreResponse.from(saved), uriInfo))
                .build();
    }

    @GET
    @Path("/{id}")
    @HasPermission(resource = ResourceType.STORE, action = PbacAction.READ)
    @Operation(summary = "Détails d'une boutique", description = "Récupère les informations d'une boutique par son ID")
    public Response getById(@PathParam("id") UUID id) {
        return storeRepository.findById(id)
                .map(StoreResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PATCH
    @Path("/{id}")
    @HasPermission(resource = ResourceType.STORE, action = PbacAction.UPDATE, checkOwnership = true)
    @Operation(summary = "Modifier une boutique", description = "Met à jour les informations. Réservé au propriétaire.")
    @APIResponse(responseCode = "200", description = "Boutique mise à jour")
    @APIResponse(responseCode = "403", description = "Vous n'êtes pas le propriétaire")
    public Response update(@PathParam("id") UUID id, @Valid StoreCreateRequest request) {
        return storeRepository.findById(id)
                .map(store -> {
                    store.setName(request.name());
                    store.setDescription(request.description());
                    var saved = storeRepository.save(store);
                    return Response.ok(hateoasMapper.toResource(StoreResponse.from(saved), uriInfo)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
