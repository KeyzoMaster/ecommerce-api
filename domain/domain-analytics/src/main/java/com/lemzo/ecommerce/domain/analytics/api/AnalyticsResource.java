package com.lemzo.ecommerce.domain.analytics.api;

import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.domain.analytics.api.dto.AnalyticsDashboardResponse;
import com.lemzo.ecommerce.domain.analytics.service.AnalyticsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Ressource JAX-RS pour la consultation des analytics.
 */
@Path("/analytics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Analytics", description = "Statistiques et rapports de ventes")
@SecurityRequirement(name = "jwt")
public class AnalyticsResource {

    @Inject
    private AnalyticsService analyticsService;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/dashboard")
    @HasPermission(resource = ResourceType.ANALYTICS, action = PbacAction.VIEW_ANALYTICS)
    @Operation(summary = "Obtenir les stats du dashboard", description = "Retourne les tendances avec liens hypermédias")
    public Response getDashboard() {
        AnalyticsDashboardResponse stats = analyticsService.getDashboardStats();
        return Response.ok(hateoasMapper.toResource(stats, uriInfo)).build();
    }

    @GET
    @Path("/export/products/csv")
    @Produces("text/csv; charset=UTF-8")
    @HasPermission(resource = ResourceType.ANALYTICS, action = PbacAction.VIEW_ANALYTICS)
    @Operation(summary = "Exporter le top produits", description = "Génère un rapport CSV des produits les plus vendus")
    public Response exportProductsCsv() {
        String csv = analyticsService.exportTopProductsToCsv();
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"top_produits.csv\"")
                .build();
    }

    @GET
    @Path("/export/sales/csv")
    @Produces("text/csv; charset=UTF-8")
    @HasPermission(resource = ResourceType.ANALYTICS, action = PbacAction.VIEW_ANALYTICS)
    @Operation(summary = "Exporter les ventes", description = "Génère un rapport CSV des ventes quotidiennes")
    public Response exportSalesCsv() {
        String csv = analyticsService.exportDailyTrendsToCsv();
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"ventes_journalieres.csv\"")
                .build();
    }
}
