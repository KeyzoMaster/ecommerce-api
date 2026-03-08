package com.lemzo.ecommerce.iam.api;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.iam.api.dto.PermissionResponse;
import com.lemzo.ecommerce.iam.api.dto.RoleCreateRequest;
import com.lemzo.ecommerce.iam.api.dto.RoleResponse;
import com.lemzo.ecommerce.iam.service.RoleManagementService;
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
import java.util.stream.Collectors;

/**
 * Ressource pour la gestion administrative des rôles et permissions.
 */
@Path("/iam/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Administration IAM", description = "Gestion des rôles et permissions du système")
@SecurityRequirement(name = "jwt")
public class RoleResource {

    @Inject
    private RoleManagementService roleManagementService;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/permissions")
    @HasPermission(resource = ResourceType.IAM, action = PbacAction.READ)
    @Operation(summary = "Lister toutes les permissions", description = "Retourne la liste des actions possibles par ressource")
    public Response listPermissions() {
        var permissions = roleManagementService.getAllPermissions().stream()
                .map(PermissionResponse::from)
                .collect(Collectors.toList());
        return Response.ok(hateoasMapper.toResource(permissions, uriInfo)).build();
    }

    @GET
    @HasPermission(resource = ResourceType.IAM, action = PbacAction.READ)
    @Operation(summary = "Lister les rôles", description = "Retourne tous les rôles configurés (système et personnalisés)")
    public Response listRoles() {
        var roles = roleManagementService.getAllRoles().stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
        return Response.ok(hateoasMapper.toResource(roles, uriInfo)).build();
    }

    @POST
    @HasPermission(resource = ResourceType.IAM, action = PbacAction.MANAGE)
    @Operation(summary = "Créer un rôle personnalisé", description = "Définit un nouveau rôle avec un ensemble de permissions")
    @APIResponse(responseCode = "201", description = "Rôle créé avec succès")
    @APIResponse(responseCode = "400", description = "Tentative d'escalade de privilèges ou données invalides")
    public Response createRole(@Valid RoleCreateRequest request) {
        var role = roleManagementService.createRole(request);
        var res = RoleResponse.from(role);
        return Response.status(Response.Status.CREATED)
                .entity(hateoasMapper.toResource(res, uriInfo))
                .build();
    }
}
