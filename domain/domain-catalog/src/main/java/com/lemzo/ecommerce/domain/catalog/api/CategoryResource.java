package com.lemzo.ecommerce.domain.catalog.api;
import jakarta.enterprise.context.ApplicationScoped;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.domain.catalog.api.dto.CategoryCreateRequest;
import com.lemzo.ecommerce.domain.catalog.api.dto.CategoryResponse;
import com.lemzo.ecommerce.domain.catalog.service.CatalogService;
import jakarta.enterprise.context.RequestScoped;
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
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

/**
 * Ressource pour la gestion des catégories de produits.
 */
@Path("/catalog/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Catalogue : Catégories", description = "Gestion des catégories du catalogue")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@RequestScoped
public class CategoryResource {

    private final CatalogService catalogService;
    private final HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Lister les catégories", description = "Retourne l'arborescence complète des catégories")
    @APIResponse(responseCode = "200", description = "Liste récupérée")
    public Response list() {
        final var responses = catalogService.getAllCategories().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
        return Response.ok(hateoasMapper.toResource(responses, uriInfo)).build();
    }

    @POST
    @Operation(summary = "Créer une catégorie", description = "Ajoute une nouvelle catégorie (Nécessite CATALOG:CREATE)")
    @SecurityRequirement(name = "jwt")
    @APIResponse(responseCode = "201", description = "Catégorie créée")
    @APIResponse(responseCode = "403", description = "Permission insuffisante")
    @HasPermission(resource = ResourceType.CATALOG, action = PbacAction.CREATE)
    public Response create(@Valid final CategoryCreateRequest request) {
        final var category = catalogService.createCategory(
                request.name(), request.slug(), request.description(), request.parentId());
        return Response.status(Response.Status.CREATED)
                .entity(hateoasMapper.toResource(CategoryResponse.from(category), uriInfo))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Détails d'une catégorie")
    @APIResponse(responseCode = "200", description = "Détails trouvés")
    @APIResponse(responseCode = "404", description = "Catégorie inexistante")
    public Response getById(@Parameter(description = "UUID de la catégorie") @PathParam("id") final UUID id) {
        return catalogService.findCategoryById(id)
                .map(CategoryResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
