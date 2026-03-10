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

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

/**
 * Ressource pour le suivi des expéditions.
 */
@Path("/shipping")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Expéditions", description = "Suivi des colis et livraisons")
@SecurityRequirement(name = "jwt")
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
    @Operation(summary = "Suivre une expédition", description = "Récupère les infos de livraison via l'ID de commande")
    @APIResponse(responseCode = "200", description = "Expédition trouvée")
    @APIResponse(responseCode = "404", description = "Pas d'expédition pour cette commande")
    public Response getByOrder(@Parameter(description = "ID de la commande") @PathParam("orderId") final UUID orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .map(ShipmentResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PATCH
    @Path("/{trackingNumber}/status")
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.MANAGE)
    @Operation(summary = "Mettre à jour le statut", description = "Modifie l'état d'avancement du colis (Nécessite PLATFORM:MANAGE)")
    @APIResponse(responseCode = "200", description = "Statut mis à jour")
    public Response updateStatus(@Parameter(description = "Numéro de suivi") @PathParam("trackingNumber") final String trackingNumber, 
                                 @Parameter(description = "Nouveau statut") @QueryParam("status") final Shipment.ShipmentStatus status) {
        final var shipment = shippingService.updateStatus(trackingNumber, status);
        return Response.ok(ShipmentResponse.from(shipment)).build();
    }
}
