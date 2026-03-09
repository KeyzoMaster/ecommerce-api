package com.lemzo.ecommerce.iam.api;

import com.lemzo.ecommerce.core.api.hateoas.HateoasMapper;
import com.lemzo.ecommerce.iam.service.AuthenticationService;
import com.lemzo.ecommerce.iam.service.UserService;
import com.lemzo.ecommerce.security.infrastructure.jwt.JwtService;
import com.lemzo.ecommerce.security.infrastructure.jwt.TokenRevocationService;
import io.jsonwebtoken.Claims;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthResource (Focus sur Logout et Révocation).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthResource Unit Tests")
class AuthResourceTest {

    @Mock
    private AuthenticationService authService;

    @Mock
    private TokenRevocationService revocationService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private HateoasMapper hateoasMapper;

    @InjectMocks
    private AuthResource authResource;

    @Test
    @DisplayName("Should successfully logout and revoke token")
    void shouldLogoutAndRevoke() {
        // Arrange
        final String authHeader = "Bearer my-access-token";
        final String token = "my-access-token";
        final String jti = "jti-123";
        final Claims claims = mock(Claims.class);
        
        when(claims.getId()).thenReturn(jti);
        // Expiration dans 1 heure (3600s)
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        when(jwtService.validateToken(token)).thenReturn(claims);

        // Act
        final Response response = authResource.logout(authHeader);

        // Assert
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(revocationService).revoke(eq(jti), anyLong());
    }

    @Test
    @DisplayName("Logout should return 204 even if no token is provided")
    void shouldHandleLogoutWithoutToken() {
        // Act
        final Response response = authResource.logout(null);

        // Assert
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verifyNoInteractions(revocationService);
    }
}
