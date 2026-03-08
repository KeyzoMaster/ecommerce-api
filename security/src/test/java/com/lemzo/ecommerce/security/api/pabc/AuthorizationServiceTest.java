package com.lemzo.ecommerce.security.api.pabc;

import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
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

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationService Unit Tests (JUnit 6)")
class AuthorizationServiceTest {

    @Mock
    private Instance<OwnershipProvider> ownershipProviders;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthorizationService authorizationService;

    private UUID userId;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should grant access if user is Super Admin")
    void shouldGrantAccessForSuperAdmin() {
        principal = new UserPrincipal(userId, "admin@test.com", Set.of("platform:manage"));
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.PRODUCT, PbacAction.DELETE);

        assertTrue(authorized, "Super Admin should bypass all checks");
    }

    @Test
    @DisplayName("Should grant access via direct permission")
    void shouldGrantAccessViaDirectPermission() {
        principal = new UserPrincipal(userId, "user@test.com", Set.of("catalog:read"));
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.CATALOG, PbacAction.READ);

        assertTrue(authorized, "User should have direct access to CATALOG:READ");
    }

    @Test
    @DisplayName("Should grant access via implicit permission (UPDATE -> READ)")
    void shouldGrantAccessViaImplicitPermission() {
        principal = new UserPrincipal(userId, "user@test.com", Set.of("catalog:update"));
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.CATALOG, PbacAction.READ);

        assertTrue(authorized, "CATALOG:UPDATE should implicitly grant CATALOG:READ");
    }

    @Test
    @DisplayName("Should grant access via hierarchy (CATALOG -> PRODUCT)")
    void shouldGrantAccessViaHierarchy() {
        principal = new UserPrincipal(userId, "user@test.com", Set.of("catalog:read"));
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.PRODUCT, PbacAction.READ);

        assertTrue(authorized, "Permission on parent resource should be inherited by child resource");
    }

    @Test
    @DisplayName("Should deny access if permission is missing")
    void shouldDenyAccessIfPermissionMissing() {
        principal = new UserPrincipal(userId, "user@test.com", Set.of("catalog:read"));
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.CATALOG, PbacAction.DELETE);

        assertFalse(authorized, "User should not have access to CATALOG:DELETE");
    }

    @Test
    @DisplayName("Should grant access if user is owner of the resource")
    void shouldGrantAccessForOwner() {
        UUID productId = UUID.randomUUID();
        principal = new UserPrincipal(userId, "owner@test.com", Set.of("product:update"));
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        OwnershipProvider mockProvider = mock(OwnershipProvider.class);
        when(mockProvider.getResourceType()).thenReturn(ResourceType.PRODUCT);
        when(mockProvider.getOwnerId(productId)).thenReturn(userId);
        when(ownershipProviders.stream()).thenReturn(Stream.of(mockProvider));

        boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.PRODUCT, PbacAction.UPDATE, productId);

        assertTrue(authorized, "Owner should be granted access when checkOwnership is enabled");
    }

    @Test
    @DisplayName("Should deny access if user is NOT owner of the resource")
    void shouldDenyAccessForNonOwner() {
        UUID productId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();
        principal = new UserPrincipal(userId, "non-owner@test.com", Set.of("product:update"));
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        OwnershipProvider mockProvider = mock(OwnershipProvider.class);
        when(mockProvider.getResourceType()).thenReturn(ResourceType.PRODUCT);
        when(mockProvider.getOwnerId(productId)).thenReturn(differentUserId);
        when(ownershipProviders.stream()).thenReturn(Stream.of(mockProvider));

        boolean authorized = authorizationService.isAuthorized(securityContext, ResourceType.PRODUCT, PbacAction.UPDATE, productId);

        assertFalse(authorized, "Non-owner should be denied access even with PBAC permission if checkOwnership is required");
    }
}
