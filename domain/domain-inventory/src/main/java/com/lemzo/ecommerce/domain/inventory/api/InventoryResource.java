package com.lemzo.ecommerce.domain.inventory.api;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.domain.inventory.api.dto.StockResponse;
import com.lemzo.ecommerce.domain.inventory.api.dto.StockUpdateRequest;
import com.lemzo.ecommerce.domain.inventory.service.InventoryService;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ressource pour la gestion des stocks.
 */
@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Inventaire", description = "Gestion des stocks produits")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class InventoryResource {

    private final InventoryService inventoryService;
    private final HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/{productId}")
    @Operation(summary = "Consulter le stock d'un produit")
    public Response getStock(@PathParam("productId") final UUID productId) {
        return inventoryService.getStockByProduct(productId)
                .map(StockResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/low-stock")
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.READ)
    @Operation(summary = "Lister les produits en rupture ou stock faible")
    public Response getLowStocks() {
        final var lowStocks = inventoryService.getLowStocks().stream()
                .map(StockResponse::from)
                .collect(Collectors.toList());
        return Response.ok(hateoasMapper.toResource(lowStocks, uriInfo)).build();
    }

    @PUT
    @Path("/{productId}")
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.UPDATE)
    @Operation(summary = "Mettre à jour manuellement le stock")
    public Response updateStock(@PathParam("productId") final UUID productId, @Valid final StockUpdateRequest request) {
        final var stock = inventoryService.setStock(productId, request.quantity(), request.lowStockThreshold());
        return Response.ok(hateoasMapper.toResource(StockResponse.from(stock), uriInfo)).build();
    }
}
