package com.lemzo.ecommerce.domain.sales.api;

import com.lemzo.ecommerce.core.api.dto.PagedRestResponse;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.domain.sales.api.dto.OrderCreateRequest;
import com.lemzo.ecommerce.domain.sales.api.dto.OrderResponse;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import com.lemzo.ecommerce.domain.sales.service.SalesService;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

/**
 * Ressource pour la gestion des commandes.
 */
@Path("/sales/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ventes : Commandes", description = "Passage et suivi des commandes")
@SecurityRequirement(name = "jwt")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class OrderResource {

    private final SalesService salesService;
    private final OrderRepository orderRepository;
    private final HateoasMapper hateoasMapper;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Lister mes commandes", description = "Récupère l'historique des commandes de l'utilisateur connecté")
    @APIResponse(responseCode = "200", description = "Liste des commandes récupérée")
    public Response list(@Parameter(description = "Numéro de page") @QueryParam("page") @DefaultValue("1") final int page,
                         @Parameter(description = "Taille de page") @QueryParam("size") @DefaultValue("10") final int size) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var ordersPage = orderRepository.findByUserId(principal.getUserId(), PageRequest.ofPage(page, size, true));
        
        final var data = ordersPage.content().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());

        final var baseUrl = uriInfo.getAbsolutePath().toString();
        return Response.ok(PagedRestResponse.from(ordersPage, data, baseUrl)).build();
    }

    @POST
    @Operation(summary = "Passer une commande", description = "Valide le panier actuel et crée une commande")
    @APIResponse(responseCode = "201", description = "Commande créée avec succès")
    @APIResponse(responseCode = "400", description = "Données invalides ou panier vide")
    public Response checkout(@Valid final OrderCreateRequest request) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        
        final var order = salesService.placeOrder(
                principal.getUserId(), 
                request.shippingAddressId(), 
                request.couponCode(), 
                request.paymentProvider()
        );
        
        final var orderData = OrderResponse.from(order);
        
        return Response.status(Response.Status.CREATED)
                .entity(hateoasMapper.toResource(orderData, uriInfo))
                .build();
    }

    @GET
    @Path("/{orderNumber}")
    @Operation(summary = "Détails d'une commande", description = "Récupère les détails via le numéro de commande")
    @APIResponse(responseCode = "200", description = "Commande trouvée")
    @APIResponse(responseCode = "404", description = "Commande inexistante")
    public Response getByNumber(@Parameter(description = "Numéro de commande (ex: ORD-...)") @PathParam("orderNumber") final String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(OrderResponse::from)
                .map(data -> hateoasMapper.toResource(data, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/store/{storeId}")
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.READ, checkOwnership = true)
    @Operation(summary = "Lister les commandes d'une boutique (pour propriétaire)")
    public Response listByStore(@PathParam("storeId") final UUID storeId,
                                @QueryParam("page") @DefaultValue("1") final int page,
                                @QueryParam("size") @DefaultValue("10") final int size) {
        final var ordersPage = orderRepository.findByStoreId(storeId, PageRequest.ofPage(page, size, true));
        final var data = ordersPage.content().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
        
        final var baseUrl = uriInfo.getAbsolutePath().toString();
        return Response.ok(PagedRestResponse.from(ordersPage, data, baseUrl)).build();
    }

    @PATCH
    @Path("/{orderId}/status")
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.MANAGE)
    @Operation(summary = "Mettre à jour le statut d'une commande")
    public Response updateStatus(@PathParam("orderId") final UUID orderId, @QueryParam("status") final Order.OrderStatus status) {
        final var updated = salesService.updateStatus(orderId, status);
        return Response.ok(OrderResponse.from(updated)).build();
    }
}
