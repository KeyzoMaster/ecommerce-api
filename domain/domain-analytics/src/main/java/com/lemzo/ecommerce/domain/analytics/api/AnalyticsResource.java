package com.lemzo.ecommerce.domain.analytics.api;
import jakarta.enterprise.context.ApplicationScoped;

import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.domain.analytics.service.AnalyticsService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

/**
 * Ressource pour la consultation des statistiques.
 */
@Path("/analytics")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Analyses", description = "Tableaux de bord et rapports (Nécessite ANALYTICS:READ)")
@SecurityRequirement(name = "jwt")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@RequestScoped
public class AnalyticsResource {

    private final AnalyticsService analyticsService;

    @GET
    @Path("/dashboard")
    @Operation(summary = "Récupérer les stats du tableau de bord", description = "Retourne le chiffre d'affaires et le top produits")
    @APIResponse(responseCode = "200", description = "Tableau de bord récupéré")
    @APIResponse(responseCode = "403", description = "Accès refusé")
    @HasPermission(resource = ResourceType.ANALYTICS, action = PbacAction.READ)
    public Response getDashboard() {
        return Response.ok(analyticsService.getDashboard()).build();
    }

    @GET
    @Path("/export/top-products")
    @Produces("text/csv")
    @Operation(summary = "Exporter le top produits en CSV")
    @APIResponse(responseCode = "200", description = "Fichier CSV généré")
    @HasPermission(resource = ResourceType.ANALYTICS, action = PbacAction.READ)
    public Response exportTopProducts() {
        final var csv = analyticsService.exportTopProductsCsv();
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"top_products.csv\"")
                .build();
    }

    @GET
    @Path("/export/daily-trends")
    @Produces("text/csv")
    @HasPermission(resource = ResourceType.ANALYTICS, action = PbacAction.READ)
    @Operation(summary = "Exporter les tendances quotidiennes en CSV")
    @APIResponse(responseCode = "200", description = "Fichier CSV généré")
    public Response exportDailyTrends() {
        final var csv = analyticsService.exportDailyTrendsCsv();
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"daily_trends.csv\"")
                .build();
    }
}
