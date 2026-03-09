package com.lemzo.ecommerce.domain.analytics.api;

import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.domain.analytics.service.AnalyticsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Ressource pour la consultation des statistiques.
 */
@Path("/analytics")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Analytics", description = "Tableaux de bord et rapports")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AnalyticsResource {

    private final AnalyticsService analyticsService;

    @GET
    @Path("/dashboard")
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.VIEW_ANALYTICS)
    @Operation(summary = "Récupérer les stats du tableau de bord")
    public Response getDashboard() {
        return Response.ok(analyticsService.getDashboard()).build();
    }

    @GET
    @Path("/export/top-products")
    @Produces("text/csv")
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.VIEW_ANALYTICS)
    @Operation(summary = "Exporter le top produits en CSV")
    public Response exportTopProducts() {
        final var csv = analyticsService.exportTopProductsCsv();
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"top_products.csv\"")
                .build();
    }

    @GET
    @Path("/export/daily-trends")
    @Produces("text/csv")
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.VIEW_ANALYTICS)
    @Operation(summary = "Exporter les tendances quotidiennes en CSV")
    public Response exportDailyTrends() {
        final var csv = analyticsService.exportDailyTrendsCsv();
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"daily_trends.csv\"")
                .build();
    }
}
