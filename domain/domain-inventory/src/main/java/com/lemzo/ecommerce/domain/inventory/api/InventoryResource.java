package com.lemzo.ecommerce.domain.inventory.api;

import com.lemzo.ecommerce.core.api.dto.RestResponse;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.domain.inventory.api.dto.StockResponse;
import com.lemzo.ecommerce.domain.inventory.api.dto.StockUpdateRequest;
import com.lemzo.ecommerce.domain.inventory.service.InventoryService;
import com.lemzo.ecommerce.domain.inventory.repository.StockRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;

/**
 * Ressource JAX-RS pour la gestion des stocks.
 */
@Path("/inventory/stocks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Inventaire", description = "Gestion des niveaux de stock")
@SecurityRequirement(name = "jwt")
public class InventoryResource {

    @Inject
    private InventoryService inventoryService;

    @Inject
    private StockRepository stockRepository;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/{productId}")
    @HasPermission(resource = ResourceType.INVENTORY, action = PbacAction.READ)
    @Operation(summary = "Consulter le stock", description = "Retourne l'état du stock avec liens hypermédias")
    @APIResponse(responseCode = "200", description = "Stock trouvé")
    public Response getStock(@PathParam("productId") UUID productId) {
        return stockRepository.findByProductId(productId)
                .map(StockResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/alerts")
    @HasPermission(resource = ResourceType.INVENTORY, action = PbacAction.READ)
    @Operation(summary = "Lister les alertes stock", description = "Retourne les produits dont le stock est inférieur ou égal au seuil d'alerte")
    public Response getAlerts() {
        var lowStocks = inventoryService.getLowStocks();
        var responses = lowStocks.stream()
                .map(StockResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .toList();
        return Response.ok(responses).build();
    }

    @PATCH
    @Path("/{productId}")
    @HasPermission(resource = ResourceType.INVENTORY, action = PbacAction.UPDATE)
    @Operation(summary = "Ajuster le stock", description = "Ajoute ou retire une quantité du stock existant")
    @APIResponse(responseCode = "204", description = "Stock mis à jour")
    public Response updateStock(@PathParam("productId") UUID productId, @Valid StockUpdateRequest request) {
        inventoryService.updateStock(productId, request.quantityChange());
        return Response.noContent().build();
    }
}
