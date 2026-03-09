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
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class UserResource {

    private final UserService userService;
    private final StoragePort storagePort;
    private final HateoasMapper hateoasMapper;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/me")
    @Operation(summary = "Récupérer mon profil", description = "Retourne les informations de l'utilisateur connecté")
    public Response getMe() {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        return userService.findById(principal.getUserId())
                .map(this::buildUserResponse)
                .map(res -> Response.ok(res).build())
                .orElseThrow(() -> new ResourceNotFoundException("Profil non trouvé"));
    }

    @PUT
    @Path("/me")
    @Operation(summary = "Mettre à jour mon profil", description = "Modifie le nom et le prénom de l'utilisateur connecté")
    public Response updateProfile(@Valid final UserProfileRequest request) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var user = userService.updateProfile(principal.getUserId(), request.firstName(), request.lastName());
        return Response.ok(buildUserResponse(user)).build();
    }

    @POST
    @Path("/me/payment-methods")
    @Operation(summary = "Ajouter un moyen de paiement", description = "Enregistre une méthode de paiement mockée")
    public Response addPaymentMethod(@Valid final PaymentMethodRequest request) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var user = userService.addPaymentMethod(principal.getUserId(), request.type(), request.details());
        return Response.ok(buildUserResponse(user)).build();
    }

    @POST
    @Path("/me/addresses")
    @Operation(summary = "Ajouter une adresse", description = "Ajoute une adresse au profil de l'utilisateur")
    public Response addAddress(@Valid final AddressRequest request) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var user = userService.addAddress(principal.getUserId(), request.toEntity());
        return Response.ok(buildUserResponse(user)).build();
    }

    @PUT
    @Path("/me/addresses/{addressId}")
    @Operation(summary = "Modifier une adresse", description = "Met à jour une adresse existante via son ID")
    public Response updateAddress(@PathParam("addressId") final String addressId, @Valid final AddressRequest request) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var user = userService.updateAddress(principal.getUserId(), addressId, request.toEntity());
        return Response.ok(buildUserResponse(user)).build();
    }

    @DELETE
    @Path("/me/addresses/{addressId}")
    @Operation(summary = "Supprimer une adresse", description = "Supprime une adresse par son identifiant unique")
    public Response removeAddress(@PathParam("addressId") final String addressId) {
        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();
        final var user = userService.removeAddress(principal.getUserId(), addressId);
        return Response.ok(buildUserResponse(user)).build();
    }

    @POST
    @Path("/{id}/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.UPDATE, checkOwnership = true)
    @Operation(summary = "Uploader un avatar", description = "Enregistre l'image de profil")
    public Response uploadAvatar(@PathParam("id") final UUID id, @Context final EntityPart filePart) {
        userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        final var principal = (AuthenticatedUser) securityContext.getUserPrincipal();

        try (var input = filePart.getContent()) {
            final var path = storagePort.storePartitioned(
                    input,
                    "avatar.png",
                    filePart.getMediaType().toString(),
                    principal.getUserId(),
                    ResourceType.PLATFORM,
                    id
            );
            
            return Response.ok(Map.of("avatarPath", path)).build();
        } catch (final IOException e) {
            throw new RuntimeException("Erreur lors de l'upload de l'avatar", e);
        }
    }

    private RestResponse<UserResponse> buildUserResponse(final User user) {
        return hateoasMapper.toResource(UserResponse.from(user), uriInfo);
    }
}
