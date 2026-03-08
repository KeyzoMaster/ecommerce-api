package com.lemzo.ecommerce.domain.catalog.api;

import com.lemzo.ecommerce.core.api.dto.Link;
import com.lemzo.ecommerce.core.api.dto.RestResponse;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.domain.catalog.api.dto.CategoryCreateRequest;
import com.lemzo.ecommerce.domain.catalog.api.dto.CategoryResponse;
import com.lemzo.ecommerce.domain.catalog.domain.Category;
import com.lemzo.ecommerce.domain.catalog.service.CatalogService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ressource JAX-RS pour la gestion des catégories.
 */
@Path("/catalog/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Catalogue", description = "Gestion des produits et catégories")
public class CategoryResource {

    @Inject
    private CatalogService catalogService;

    @Context
    private UriInfo uriInfo;

    @GET
    @HasPermission(resource = ResourceType.CATALOG, action = PbacAction.READ)
    @Operation(summary = "Lister les catégories", description = "Retourne la liste complète des catégories")
    public Response list() {
        var categories = catalogService.getAllCategories();
        var responses = categories.stream()
                .map(this::buildCategoryResource)
                .toList();
        return Response.ok(responses).build();
    }

    @POST
    @HasPermission(resource = ResourceType.CATALOG, action = PbacAction.CREATE)
    @Operation(summary = "Créer une catégorie")
    public Response create(@Valid CategoryCreateRequest request) {
        var category = catalogService.createCategory(
                request.name(), request.slug(), request.description(), request.parentId()
        );
        return Response.status(Response.Status.CREATED)
                .entity(buildCategoryResource(category))
                .build();
    }

    @GET
    @Path("/{id}")
    @HasPermission(resource = ResourceType.CATALOG, action = PbacAction.READ)
    @Operation(summary = "Détails d'une catégorie")
    public Response get(@PathParam("id") UUID id) {
        return catalogService.getCategoryById(id)
                .map(this::buildCategoryResource)
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    private RestResponse<CategoryResponse> buildCategoryResource(Category category) {
        List<Link> links = new ArrayList<>();
        String selfHref = uriInfo.getBaseUriBuilder()
                .path(CategoryResource.class)
                .path(category.getId().toString())
                .build().toString();

        links.add(Link.self(selfHref));
        
        if (category.getParent() != null) {
            links.add(Link.of("parent", uriInfo.getBaseUriBuilder()
                    .path(CategoryResource.class)
                    .path(category.getParent().getId().toString())
                    .build().toString(), "GET"));
        }

        return RestResponse.of(CategoryResponse.from(category), links);
    }
}
