package com.lemzo.ecommerce.domain.shipping.api;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.domain.shipping.api.dto.ShipmentResponse;
import com.lemzo.ecommerce.domain.shipping.domain.Shipment;
import com.lemzo.ecommerce.domain.shipping.service.ShippingService;
import com.lemzo.ecommerce.domain.shipping.repository.ShipmentRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;

/**
 * Ressource JAX-RS pour le suivi des expéditions.
 */
@Path("/shipping/shipments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Expédition", description = "Suivi et mise à jour des livraisons")
@SecurityRequirement(name = "jwt")
public class ShippingResource {

    @Inject
    private ShippingService shippingService;

    @Inject
    private ShipmentRepository shipmentRepository;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/order/{orderId}")
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.READ)
    @Operation(summary = "Suivre une expédition", description = "Retourne les informations de livraison pour une commande")
    public Response getByOrder(@PathParam("orderId") UUID orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .map(ShipmentResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PATCH
    @Path("/{trackingNumber}/status")
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.UPDATE)
    @Operation(summary = "Mettre à jour le statut", description = "Change l'état de l'expédition")
    public Response updateStatus(
            @PathParam("trackingNumber") String trackingNumber,
            @QueryParam("status") Shipment.ShippingStatus status) {
        
        var shipment = shippingService.updateStatus(trackingNumber, status);
        return Response.ok(hateoasMapper.toResource(ShipmentResponse.from(shipment), uriInfo)).build();
    }
}
