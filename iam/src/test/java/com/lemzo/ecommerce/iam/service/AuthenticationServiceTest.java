package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
import com.lemzo.ecommerce.security.infrastructure.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests (JUnit 6)")
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authService;

    @Test
    @DisplayName("Should successfully login")
    void shouldLoginSuccessfully() {
        // Arrange
        String identifier = "jdoe";
        String password = "password123";
        User user = new User(identifier, "jdoe@test.com", "hashed");
        user.setId(UUID.randomUUID());
        user.setEnabled(true);

        when(userService.findByIdentifier(identifier)).thenReturn(Optional.of(user));
        when(passwordService.verify(anyString(), any())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("mock_token");

        // Act
        AuthenticationService.LoginResult result = authService.login(identifier, password);

        // Assert
        assertNotNull(result);
        assertEquals("mock_token", result.accessToken());
        assertEquals(user, result.user());
    }

    @Test
    @DisplayName("Should fail login if user not found")
    void shouldFailIfUserNotFound() {
        when(userService.findByIdentifier(anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> authService.login("unknown", "pass"));
    }

    @Test
    @DisplayName("Should fail login if password incorrect")
    void shouldFailIfPasswordIncorrect() {
        User user = new User("jdoe", "jdoe@test.com", "hashed");
        user.setEnabled(true);

        when(userService.findByIdentifier(anyString())).thenReturn(Optional.of(user));
        when(passwordService.verify(anyString(), any())).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> authService.login("jdoe", "wrong"));
    }

    @Test
    @DisplayName("Should fail login if account disabled")
    void shouldFailIfAccountDisabled() {
        User user = new User("jdoe", "jdoe@test.com", "hashed");
        user.setEnabled(false);

        when(userService.findByIdentifier(anyString())).thenReturn(Optional.of(user));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, 
                () -> authService.login("jdoe", "pass"));
        assertEquals("error.iam.account_disabled", ex.getMessage());
    }
}
