package com.lemzo.ecommerce.domain.marketing.api;

import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.domain.marketing.api.dto.CouponCreateRequest;
import com.lemzo.ecommerce.domain.marketing.api.dto.CouponResponse;
import com.lemzo.ecommerce.domain.marketing.repository.CouponRepository;
import com.lemzo.ecommerce.domain.marketing.service.MarketingService;
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

/**
 * Ressource JAX-RS pour la gestion du marketing.
 */
@Path("/marketing/coupons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Marketing", description = "Gestion des coupons et promotions")
@SecurityRequirement(name = "jwt")
public class CouponResource {

    @Inject
    private CouponRepository couponRepository;

    @Inject
    private MarketingService marketingService;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @POST
    @HasPermission(resource = ResourceType.MARKETING, action = PbacAction.CREATE)
    @Operation(summary = "Créer un coupon", description = "Génère un nouveau code promo avec liens HATEOAS")
    @APIResponse(responseCode = "201", description = "Coupon créé")
    public Response create(@Valid CouponCreateRequest request) {
        var coupon = marketingService.createCoupon(request);
        var res = hateoasMapper.toResource(CouponResponse.from(coupon), uriInfo);
        return Response.status(Response.Status.CREATED).entity(res).build();
    }

    @GET
    @Path("/{code}")
    @HasPermission(resource = ResourceType.MARKETING, action = PbacAction.READ)
    @Operation(summary = "Vérifier un coupon", description = "Détails d'un coupon avec liens hypermédias")
    public Response getCoupon(@PathParam("code") String code) {
        return couponRepository.findByCode(code)
                .map(CouponResponse::from)
                .map(res -> hateoasMapper.toResource(res, uriInfo))
                .map(res -> Response.ok(res).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
