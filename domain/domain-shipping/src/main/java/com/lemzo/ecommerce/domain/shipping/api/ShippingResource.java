package com.lemzo.ecommerce.domain.shipping.api;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
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
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;

/**
 * Ressource pour le suivi des expéditions.
 */
@Path("/shipping")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Expéditions", description = "Suivi des colis et livraisons")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ShippingResource {

    private final ShippingService shippingService;
    private final ShipmentRepository shipmentRepository;
    private final HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/order/{orderId}")
    @Operation(summary = "Suivre une expédition par son ID de commande")
    public Response getByOrder(@PathParam("orderId") final UUID orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .map(ShipmentResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PATCH
    @Path("/{trackingNumber}/status")
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.MANAGE)
    @Operation(summary = "Mettre à jour le statut d'une expédition")
    public Response updateStatus(@PathParam("trackingNumber") final String trackingNumber, 
                                 @QueryParam("status") final Shipment.ShipmentStatus status) {
        final var shipment = shippingService.updateStatus(trackingNumber, status);
        return Response.ok(ShipmentResponse.from(shipment)).build();
    }
}
