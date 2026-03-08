package com.lemzo.ecommerce.domain.sales.api;

import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.dto.Link;
import com.lemzo.ecommerce.core.api.dto.RestResponse;
import com.lemzo.ecommerce.core.api.dto.PagedRestResponse;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.domain.sales.api.dto.OrderCreateRequest;
import com.lemzo.ecommerce.domain.sales.api.dto.OrderResponse;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.service.SalesService;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Ressource JAX-RS pour la gestion des commandes.
 */
@Path("/sales/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ventes", description = "Gestion des commandes et paiements")
@SecurityRequirement(name = "jwt")
public class OrderResource {

    @Inject
    private SalesService salesService;

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.READ)
    @Operation(summary = "Mes commandes", description = "Retourne l'historique des commandes avec liens de navigation")
    public Response getMyOrders(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        var ordersPage = orderRepository.findByUserId(principal.getUserId(), PageRequest.ofPage(page + 1).size(size));
        var baseUrl = uriInfo.getAbsolutePath().toString();

        return Response.ok(PagedRestResponse.from(ordersPage, baseUrl)).build();
    }

    @GET
    @Path("/shipping-methods")
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.READ)
    @Operation(summary = "Lister les modes de livraison", description = "Retourne la liste des modes de livraison disponibles")
    public Response getShippingMethods() {
        return Response.ok(salesService.getAvailableShippingMethods()).build();
    }

    @POST
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.CREATE)
    @Operation(summary = "Passer une commande", description = "Crée une commande et initialise le processus de paiement")
    @APIResponse(responseCode = "201", description = "Commande créée avec succès")
    public Response placeOrder(@Valid OrderCreateRequest request) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        OrderResponse orderData = salesService.placeOrder(principal.getUserId(), request);
        
        List<Link> links = new ArrayList<>();
        links.add(Link.self(uriInfo.getAbsolutePathBuilder().path(orderData.orderNumber()).build().toString()));
        links.add(Link.of("payment-redirect", orderData.paymentUrl(), "GET"));

        return Response.status(Response.Status.CREATED)
                .entity(RestResponse.of(orderData, links))
                .build();
    }

    @GET
    @Path("/{orderNumber}")
    @HasPermission(resource = ResourceType.ORDER, action = PbacAction.READ, checkOwnership = true)
    @Operation(summary = "Récupérer une commande", description = "Retourne les détails d'une commande. Réservé à l'acheteur.")
    @APIResponse(responseCode = "200", description = "Commande trouvée")
    public Response getByOrderNumber(@PathParam("orderNumber") String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(order -> {
                    var data = OrderResponse.from(order, null);
                    return hateoasMapper.toResource(data, uriInfo);
                })
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/store/{storeId}")
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.MANAGE)
    @Operation(summary = "Commandes de la boutique", description = "Liste les commandes contenant au moins un produit de cette boutique.")
    public Response getStoreOrders(@PathParam("storeId") java.util.UUID storeId) {
        return Response.ok(salesService.getOrdersByStore(storeId)).build();
    }

    @PATCH
    @Path("/{orderId}/status")
    @HasPermission(resource = ResourceType.SALES, action = PbacAction.MANAGE)
    @Operation(summary = "Mettre à jour le statut", description = "Permet au vendeur de changer l'état d'une commande (ex: EXPÉDIÉE).")
    public Response updateStatus(@PathParam("orderId") java.util.UUID orderId, @QueryParam("status") Order.OrderStatus status) {
        return Response.ok(salesService.updateOrderStatus(orderId, status)).build();
    }
}
