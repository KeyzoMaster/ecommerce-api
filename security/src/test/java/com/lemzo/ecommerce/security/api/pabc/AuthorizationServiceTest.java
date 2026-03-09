package com.lemzo.ecommerce.security.api.pabc;

import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.security.OwnershipProvider;
import com.lemzo.ecommerce.domain.core.iam.UserPort;
import com.lemzo.ecommerce.security.infrastructure.pabc.UserPrincipal;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthorizationService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationService Unit Tests")
class AuthorizationServiceTest {

    @Mock
    private Instance<OwnershipProvider> ownershipProviders;

    @Mock
    private UserPort userPort;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthorizationService authorizationService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should grant access if user is Super Admin")
    void shouldGrantAccessForSuperAdmin() {
        final var principal = new UserPrincipal(userId, "admin@test.com", Set.of());
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(userPort.getPermissions(userId)).thenReturn(Set.of("platform:manage"));

        final boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.PRODUCT, PbacAction.DELETE);

        assertTrue(authorized, "Super Admin should bypass all checks");
    }

    @Test
    @DisplayName("Should grant access via direct permission")
    void shouldGrantAccessViaDirectPermission() {
        final var principal = new UserPrincipal(userId, "user@test.com", Set.of());
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(userPort.getPermissions(userId)).thenReturn(Set.of("catalog:read"));

        final boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.CATALOG, PbacAction.READ);

        assertTrue(authorized, "User should have direct access to CATALOG:READ");
    }

    @Test
    @DisplayName("Should grant access if user is owner of the resource")
    void shouldGrantAccessForOwner() {
        final var productId = UUID.randomUUID();
        final var principal = new UserPrincipal(userId, "owner@test.com", Set.of());
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(userPort.getPermissions(userId)).thenReturn(Set.of("product:update"));

        final var mockProvider = mock(OwnershipProvider.class);
        when(mockProvider.getResourceType()).thenReturn(ResourceType.PRODUCT);
        when(mockProvider.getOwnerId(productId)).thenReturn(userId);
        when(ownershipProviders.stream()).thenReturn(Stream.of(mockProvider));

        final boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.PRODUCT, PbacAction.UPDATE, productId);

        assertTrue(authorized, "Owner should be granted access when checkOwnership is enabled");
    }
}
