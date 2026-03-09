package com.lemzo.ecommerce.domain.sales.api;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.domain.sales.domain.Cart;
import com.lemzo.ecommerce.domain.sales.service.CartService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Ressource pour la gestion du panier utilisateur.
 */
@Path("/sales/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ventes : Panier", description = "Gestion du panier d'achat")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class CartResource {

    private final CartService cartService;
    private final HateoasMapper hateoasMapper;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Récupérer mon panier")
    public Response getCart() {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var cart = cartService.getCart(principal.getUserId()).orElse(new Cart(principal.getUserId(), java.util.List.of()));
        return Response.ok(hateoasMapper.toResource(cart, uriInfo)).build();
    }

    @POST
    @Path("/items")
    @Operation(summary = "Ajouter un produit au panier")
    public Response addItem(@QueryParam("productId") final java.util.UUID productId, 
                            @QueryParam("quantity") @DefaultValue("1") final int quantity) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var cart = cartService.addToCart(principal.getUserId(), productId, quantity);
        return Response.ok(hateoasMapper.toResource(cart, uriInfo)).build();
    }

    @DELETE
    @Path("/items/{productId}")
    @Operation(summary = "Retirer un produit du panier")
    public Response removeItem(@PathParam("productId") final java.util.UUID productId) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var cart = cartService.removeFromCart(principal.getUserId(), productId);
        return Response.ok(hateoasMapper.toResource(cart, uriInfo)).build();
    }

    @DELETE
    @Operation(summary = "Vider le panier")
    public Response clearCart() {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        cartService.clearCart(principal.getUserId());
        return Response.noContent().build();
    }
}
