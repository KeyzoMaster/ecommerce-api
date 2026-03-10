package com.lemzo.ecommerce.domain.marketing.api;
import com.lemzo.ecommerce.domain.marketing.domain.Coupon;
import jakarta.enterprise.context.ApplicationScoped;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.domain.marketing.api.dto.CouponCreateRequest;
import com.lemzo.ecommerce.domain.marketing.api.dto.CouponResponse;
import com.lemzo.ecommerce.domain.marketing.service.MarketingService;
import jakarta.enterprise.context.RequestScoped;
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
import java.math.BigDecimal;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

/**
 * Ressource pour la gestion des coupons.
 */
@Path("/marketing/coupons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Marketing", description = "Gestion des coupons et promotions")
@SecurityRequirement(name = "jwt")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@RequestScoped
public class CouponResource {

    private final MarketingService marketingService;
    private final HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @POST
    @Operation(summary = "Créer un nouveau coupon", description = "Ajoute un coupon de réduction (Nécessite COUPON:CREATE)")
    @APIResponse(responseCode = "201", description = "Coupon créé")
    @APIResponse(responseCode = "403", description = "Permission insuffisante")
    @HasPermission(resource = ResourceType.COUPON, action = PbacAction.CREATE)
    public Response create(@Valid final CouponCreateRequest request) {
        final var coupon = marketingService.createCoupon(request);
        final var res = CouponResponse.from(coupon);
        return Response.status(Response.Status.CREATED)
                .entity(hateoasMapper.toResource(res, uriInfo))
                .build();
    }

    @GET
    @Path("/{code}")
    @Operation(summary = "Vérifier la validité d'un coupon", description = "Vérifie si un code coupon est actif et applicable")
    @APIResponse(responseCode = "200", description = "Coupon valide")
    @APIResponse(responseCode = "404", description = "Coupon invalide ou expiré")
    public Response validate(@Parameter(description = "Code du coupon") @PathParam("code") final String code) {
        return marketingService.findCouponByCode(code)
                .filter(Coupon::isValid)
                .map(_ -> Response.ok().build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
