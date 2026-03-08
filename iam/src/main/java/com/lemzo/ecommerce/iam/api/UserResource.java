package com.lemzo.ecommerce.iam.api;

import com.lemzo.ecommerce.core.api.dto.RestResponse;
import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.api.exception.ResourceNotFoundException;
import com.lemzo.ecommerce.core.contract.storage.StoragePort;
import com.lemzo.ecommerce.core.domain.Address;
import com.lemzo.ecommerce.iam.api.dto.AddressRequest;
import com.lemzo.ecommerce.iam.api.dto.PaymentMethodRequest;
import com.lemzo.ecommerce.iam.api.dto.UserProfileRequest;
import com.lemzo.ecommerce.iam.api.dto.UserResponse;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Ressource pour la gestion des profils utilisateurs.
 */
@Path("/iam/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Utilisateurs", description = "Gestion du profil et des préférences")
@SecurityRequirement(name = "jwt")
public class UserResource {

    @Inject
    private UserService userService;

    @Inject
    private StoragePort storagePort;

    @Inject
    private HateoasMapper hateoasMapper;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/me")
    @Operation(summary = "Récupérer mon profil", description = "Retourne les informations de l'utilisateur connecté")
    public Response getMe() {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        return userService.findById(principal.getUserId())
                .map(user -> Response.ok(buildUserResource(user)).build())
                .orElseThrow(() -> new ResourceNotFoundException("Profil non trouvé"));
    }

    @PUT
    @Path("/me")
    @Operation(summary = "Mettre à jour mon profil", description = "Modifie le nom et le prénom de l'utilisateur connecté")
    public Response updateProfile(@Valid UserProfileRequest request) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        User user = userService.updateProfile(principal.getUserId(), request.firstName(), request.lastName());
        return Response.ok(buildUserResource(user)).build();
    }

    @POST
    @Path("/me/payment-methods")
    @Operation(summary = "Ajouter un moyen de paiement", description = "Enregistre une méthode de paiement mockée")
    public Response addPaymentMethod(@Valid PaymentMethodRequest request) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        User user = userService.addPaymentMethod(principal.getUserId(), request.type(), request.details());
        return Response.ok(buildUserResource(user)).build();
    }

    @POST
    @Path("/me/addresses")
    @Operation(summary = "Ajouter une adresse", description = "Ajoute une adresse au profil de l'utilisateur")
    public Response addAddress(@Valid AddressRequest request) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        Address address = Address.builder()
                .label(request.label())
                .street(request.street())
                .city(request.city())
                .zipCode(request.zipCode())
                .country(request.country())
                .build();
        
        User user = userService.addAddress(principal.getUserId(), address);
        return Response.ok(buildUserResource(user)).build();
    }

    @PUT
    @Path("/me/addresses/{addressId}")
    @Operation(summary = "Modifier une adresse", description = "Met à jour une adresse existante via son ID")
    public Response updateAddress(@PathParam("addressId") String addressId, @Valid AddressRequest request) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        Address address = Address.builder()
                .label(request.label())
                .street(request.street())
                .city(request.city())
                .zipCode(request.zipCode())
                .country(request.country())
                .build();
        
        User user = userService.updateAddress(principal.getUserId(), addressId, address);
        return Response.ok(buildUserResource(user)).build();
    }

    @DELETE
    @Path("/me/addresses/{addressId}")
    @Operation(summary = "Supprimer une adresse", description = "Supprime une adresse par son identifiant unique")
    public Response removeAddress(@PathParam("addressId") String addressId) {
        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        User user = userService.removeAddress(principal.getUserId(), addressId);
        return Response.ok(buildUserResource(user)).build();
    }

    @POST
    @Path("/{id}/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @HasPermission(resource = ResourceType.IAM, action = PbacAction.UPDATE, checkOwnership = true)
    @Operation(summary = "Uploader un avatar", description = "Enregistre l'image de profil sur MinIO (Partitionné)")
    @APIResponse(responseCode = "200", description = "Avatar mis à jour")
    @APIResponse(responseCode = "403", description = "Accès refusé (non propriétaire)")
    public Response uploadAvatar(@PathParam("id") UUID id, @Context EntityPart filePart) {
        // Vérifier l'existence de l'utilisateur
        userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        var principal = (AuthenticatedUser) securityContext.getUserPrincipal();

        try (var input = filePart.getContent()) {
            // Partitionnement : users/{userId}/profile/avatar.png
            String path = storagePort.storePartitioned(
                    input,
                    "avatar.png",
                    filePart.getMediaType().toString(),
                    principal.getUserId(),
                    ResourceType.IAM,
                    id // ID de la ressource (l'utilisateur lui-même)
            );
            
            return Response.ok(Map.of("avatarPath", path)).build();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload de l'avatar", e);
        }
    }

    private RestResponse<UserResponse> buildUserResource(User user) {
        return hateoasMapper.toResource(UserResponse.from(user), uriInfo);
    }
}
