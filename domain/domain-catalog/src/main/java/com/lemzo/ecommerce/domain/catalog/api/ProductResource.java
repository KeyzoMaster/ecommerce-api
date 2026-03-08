package com.lemzo.ecommerce.domain.catalog.api;

import com.lemzo.ecommerce.core.api.dto.Link;
import com.lemzo.ecommerce.core.api.dto.RestResponse;
import com.lemzo.ecommerce.core.api.dto.PagedRestResponse;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.contract.storage.StoragePort;
import com.lemzo.ecommerce.util.storage.FileSanitizer;
import com.lemzo.ecommerce.util.io.SizeLimitedInputStream;
import com.lemzo.ecommerce.domain.catalog.api.dto.ProductCreateRequest;
import com.lemzo.ecommerce.domain.catalog.api.dto.ProductResponse;
import com.lemzo.ecommerce.domain.catalog.api.dto.ProductUpdateRequest;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.service.CatalogService;
import com.lemzo.ecommerce.security.api.pabc.AuthorizationService;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Ressource JAX-RS pour la gestion du catalogue.
 */
@Path("/catalog/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Catalogue", description = "Gestion des produits et catégories")
public class ProductResource {

    @Inject
    private CatalogService catalogService;

    @Inject
    private HateoasMapper hateoasMapper;

    @Inject
    private AuthorizationService authService;

    @Inject
    private StoragePort storagePort;

    @Inject
    private FileSanitizer fileSanitizer;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Lister les produits", description = "Retourne une page de produits avec recherche avancée (texte, catégorie, prix, disponibilité, tri)")
    public Response list(
            @QueryParam("q") String query,
            @QueryParam("category") UUID categoryId,
            @QueryParam("minPrice") BigDecimal minPrice,
            @QueryParam("maxPrice") BigDecimal maxPrice,
            @QueryParam("available") Boolean available,
            @QueryParam("sortBy") @DefaultValue("name") String sortBy,
            @QueryParam("sortDir") @DefaultValue("ASC") String sortDir,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        
        Sort<Product> sort = "DESC".equalsIgnoreCase(sortDir) ? 
                Sort.desc(sortBy) : Sort.asc(sortBy);
        Order<Product> order = Order.by(sort);
                
        PageRequest pageRequest = PageRequest.ofPage(page + 1).size(size);
        
        var productsPage = catalogService.searchByCriteria(query, categoryId, minPrice, maxPrice, available, pageRequest, order);

        var productResponses = productsPage.content().stream()
                .map(this::buildProductResource)
                .toList();

        var baseUrl = uriInfo.getAbsolutePath().toString();
        return Response.ok(PagedRestResponse.from(productsPage, productResponses, baseUrl)).build();
    }

    @POST
    @HasPermission(resource = ResourceType.CATALOG, action = PbacAction.CREATE)
    @Operation(summary = "Créer un nouveau produit", description = "Ajoute un produit au catalogue")
    @SecurityRequirement(name = "jwt")
    public Response create(@Valid ProductCreateRequest request) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        Product product = catalogService.createProduct(
                principal.getUserId(),
                request.name(), request.slug(), request.sku(),
                request.price(), request.categoryId(), request.attributes(),
                request.imageUrl(), request.weight(), request.shippingConfig()
        );
        
        var resource = buildProductResource(product);
        return Response.status(Response.Status.CREATED).entity(resource).build();
    }

    @PATCH
    @Path("/{id}")
    @HasPermission(resource = ResourceType.PRODUCT, action = PbacAction.UPDATE, checkOwnership = true)
    @Operation(summary = "Mettre à jour un produit", description = "Mise à jour partielle des informations d'un produit")
    @SecurityRequirement(name = "jwt")
    public Response update(@PathParam("id") UUID id, ProductUpdateRequest request) {
        Product product = catalogService.updateProduct(
                id, request.name(), request.slug(), request.sku(),
                request.description(), request.price(), request.categoryId(),
                request.active(), request.attributes(), request.imageUrl(),
                request.weight(), request.shippingConfig()
        );
        
        return Response.ok(buildProductResource(product)).build();
    }

    @GET
    @Path("/{slug}")
    @Operation(summary = "Récupérer un produit", description = "Détails d'un produit avec liens relationnels et actions autorisées")
    public Response getBySlug(@PathParam("slug") String slug) {
        return catalogService.getProductBySlug(slug)
                .map(product -> {
                    catalogService.incrementViewCount(product.getId());
                    return buildProductResource(product);
                })
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Path("/{id}/image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @HasPermission(resource = ResourceType.PRODUCT, action = PbacAction.UPDATE, checkOwnership = true)
    @Operation(summary = "Uploader une image", description = "Enregistre une image produit sur MinIO")
    public Response uploadImage(@PathParam("id") UUID id, @Context EntityPart filePart) {
        catalogService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        String originalFileName = filePart.getFileName().orElse("image.png");
        
        // 1. Sanitize filename
        String safeFileName = fileSanitizer.sanitizeFileName(originalFileName);
        
        // 2. Validate MIME type
        String contentType = filePart.getMediaType().toString();
        if (!fileSanitizer.isSafeImage(contentType)) {
            throw new BadRequestException("Type de fichier non autorisé : " + contentType);
        }
        
        try (var rawInput = filePart.getContent();
             var input = new SizeLimitedInputStream(rawInput, 5 * 1024 * 1024)) { // Max 5MB
            
            String path = storagePort.storePartitioned(
                    input, 
                    safeFileName, 
                    contentType,
                    principal.getUserId(),
                    ResourceType.PRODUCT,
                    id
            );
            
            catalogService.updateProductImage(id, path);
            
            return Response.ok(Map.of("path", path)).build();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload", e);
        }
    }

    private RestResponse<ProductResponse> buildProductResource(Product product) {
        String imageUrl = Optional.ofNullable(product.getImageUrl())
                .filter(path -> !path.isBlank())
                .map(path -> storagePort.getPresignedUrl(path, 60)) // URL valide 1h
                .orElse(product.getImageUrl());

        List<Link> links = new ArrayList<>();
        String selfHref = uriInfo.getBaseUriBuilder()
                .path(ProductResource.class)
                .path(product.getSlug())
                .build().toString();

        links.add(Link.self(selfHref));

        // Ajout dynamique des liens basés sur les permissions RÉELLES
        Optional.of(product.getId())
                .filter(id -> authService.isAuthorized(securityContext, ResourceType.PRODUCT, PbacAction.UPDATE, id))
                .ifPresent(id -> links.add(Link.of("update", selfHref, "PATCH")));

        Optional.of(product.getId())
                .filter(id -> authService.isAuthorized(securityContext, ResourceType.PRODUCT, PbacAction.DELETE, id))
                .ifPresent(id -> links.add(Link.of("delete", selfHref, "DELETE")));
        
        Optional.ofNullable(product.getCategory())
                .map(cat -> Link.of("category", uriInfo.getBaseUriBuilder()
                        .path("/catalog/categories/")
                        .path(cat.getId().toString())
                        .build().toString(), "GET"))
                .ifPresent(links::add);

        return RestResponse.of(ProductResponse.from(product, imageUrl), links);
    }
}
