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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;

/**
 * Ressource pour la gestion du panier utilisateur.
 */
@Path("/sales/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Panier", description = "Gestion des articles du panier (Redis)")
public class CartResource {

    @Inject
    private CartService cartService;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Consulter mon panier", description = "Retourne le contenu actuel du panier de l'utilisateur")
    public Response getCart() {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        Cart cart = cartService.getCart(principal.getUserId());
        return Response.ok(hateoasMapper.toResource(cart, uriInfo)).build();
    }

    @POST
    @Path("/items")
    @Operation(summary = "Ajouter un article", description = "Ajoute un produit au panier")
    public Response addItem(@QueryParam("productId") UUID productId, @QueryParam("quantity") @DefaultValue("1") int quantity) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        Cart cart = cartService.addItem(principal.getUserId(), productId, quantity);
        return Response.ok(hateoasMapper.toResource(cart, uriInfo)).build();
    }

    @DELETE
    @Path("/items/{productId}")
    @Operation(summary = "Retirer un article", description = "Supprime un produit du panier")
    public Response removeItem(@PathParam("productId") UUID productId) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        Cart cart = cartService.removeItem(principal.getUserId(), productId);
        return Response.ok(hateoasMapper.toResource(cart, uriInfo)).build();
    }

    @DELETE
    @Operation(summary = "Vider le panier", description = "Supprime tous les articles du panier")
    public Response clearCart() {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        cartService.clearCart(principal.getUserId());
        return Response.noContent().build();
    }
}
