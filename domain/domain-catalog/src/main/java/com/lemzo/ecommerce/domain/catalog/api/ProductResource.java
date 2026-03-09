package com.lemzo.ecommerce.domain.catalog.api;

import com.lemzo.ecommerce.core.api.dto.PagedRestResponse;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.contract.storage.StoragePort;
import com.lemzo.ecommerce.domain.catalog.api.dto.ProductCreateRequest;
import com.lemzo.ecommerce.domain.catalog.api.dto.ProductResponse;
import com.lemzo.ecommerce.domain.catalog.api.dto.ProductUpdateRequest;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.service.CatalogService;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ressource pour la gestion du catalogue produits.
 */
@Path("/catalog/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Catalogue : Produits", description = "Consultation et gestion des produits")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ProductResource {

    private final CatalogService catalogService;
    private final StoragePort storagePort;
    private final HateoasMapper hateoasMapper;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Lister et filtrer les produits")
    public Response list(
            @QueryParam("q") final String query,
            @QueryParam("category") final UUID categoryId,
            @QueryParam("minPrice") final BigDecimal minPrice,
            @QueryParam("maxPrice") final BigDecimal maxPrice,
            @QueryParam("available") final Boolean available,
            @QueryParam("sort") @DefaultValue("createdAt") final String sortBy,
            @QueryParam("dir") @DefaultValue("desc") final String sortDir,
            @QueryParam("page") @DefaultValue("1") final int pageNumber,
            @QueryParam("size") @DefaultValue("20") final int pageSize) {

        final PageRequest pageRequest = PageRequest.ofPage(pageNumber, pageSize, true);

        final Page<Product> productsPage = catalogService.filter(query, categoryId, minPrice, maxPrice, available, pageRequest);

        final List<ProductResponse> productResponses = productsPage.content().stream()
                .map(product -> ProductResponse.from(product, buildImageUrl(product)))
                .collect(Collectors.toList());

        final String baseUrl = uriInfo.getAbsolutePath().toString();
        return Response.ok(PagedRestResponse.from(productsPage, productResponses, baseUrl)).build();
    }

    @POST
    @HasPermission(resource = ResourceType.CATALOG, action = PbacAction.CREATE)
    @Operation(summary = "Créer un produit")
    @SecurityRequirement(name = "jwt")
    public Response create(@Valid final ProductCreateRequest request) {
        final Product product = catalogService.createProduct(
                request.name(), request.slug(), request.sku(), request.price(), request.categoryId(),
                request.attributes(), request.imageUrl(), request.weight(), request.shippingConfig()
        );

        return Response.status(Response.Status.CREATED)
                .entity(hateoasMapper.toResource(ProductResponse.from(product, buildImageUrl(product)), uriInfo))
                .build();
    }

    @GET
    @Path("/{productId}")
    @Operation(summary = "Détails d'un produit")
    public Response getById(@PathParam("productId") final UUID productId) {
        return catalogService.findById(productId)
                .map(product -> {
                    catalogService.incrementViewCount(product.getId());
                    return product;
                })
                .map(product -> ProductResponse.from(product, buildImageUrl(product)))
                .map(response -> Response.ok(hateoasMapper.toResource(response, uriInfo)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{productId}")
    @HasPermission(resource = ResourceType.PRODUCT, action = PbacAction.UPDATE, checkOwnership = true)
    @Operation(summary = "Mettre à jour un produit")
    @SecurityRequirement(name = "jwt")
    public Response update(@PathParam("productId") final UUID productId, @Valid final ProductUpdateRequest request) {
        final Product product = catalogService.updateProduct(
                productId, request.name(), request.slug(), request.sku(), request.description(),
                request.price(), request.categoryId(), request.active(), request.attributes(),
                request.imageUrl(), request.weight(), request.shippingConfig()
        );

        return Response.ok(hateoasMapper.toResource(ProductResponse.from(product, buildImageUrl(product)), uriInfo)).build();
    }

    @POST
    @Path("/{productId}/image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @HasPermission(resource = ResourceType.PRODUCT, action = PbacAction.UPDATE, checkOwnership = true)
    @Operation(summary = "Uploader une image produit")
    @SecurityRequirement(name = "jwt")
    public Response uploadImage(@PathParam("productId") final UUID productId, @Context final EntityPart filePart) {
        final AuthenticatedUser principal = (AuthenticatedUser) securityContext.getUserPrincipal();

        try (InputStream input = filePart.getContent()) {
            final String fileName = filePart.getFileName().orElse("product.jpg");
            final String contentType = filePart.getMediaType().toString();
            
            final String path = storagePort.storePartitioned(
                    input,
                    fileName,
                    contentType,
                    principal.getUserId(),
                    ResourceType.CATALOG,
                    productId
            );
            
            catalogService.updateImageUrl(productId, path);
            return Response.ok(Map.of("imagePath", path)).build();
        } catch (final IOException exception) {
            throw new RuntimeException("Erreur lors de l'upload de l'image", exception);
        }
    }

    private String buildImageUrl(final Product product) {
        return Optional.ofNullable(product.getImageUrl())
                .filter(path -> !path.startsWith("http"))
                .map(path -> storagePort.getPresignedUrl(path, 60))
                .orElse(product.getImageUrl());
    }
}
