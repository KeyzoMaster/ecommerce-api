package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.api.dto.AuthResponse;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.repository.UserRepository;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
import com.lemzo.ecommerce.security.infrastructure.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authService;

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        final String identifier = "jdoe";
        final String password = "password123";
        final User user = new User(identifier, "jdoe@test.com", "hashed_pass");
        
        final var field = com.lemzo.ecommerce.core.entity.AbstractEntity.class.getDeclaredField("entityId");
        field.setAccessible(true);
        field.set(user, UUID.randomUUID());

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));
        when(passwordService.verify(anyString(), any())).thenReturn(true);
        when(jwtService.generateToken(any(UUID.class), anyString())).thenReturn("token");

        // Act
        final AuthResponse result = authService.login(identifier, password);

        // Assert
        assertNotNull(result);
        assertEquals("token", result.accessToken());
        assertEquals("jdoe@test.com", result.email());
    }

    @Test
    @DisplayName("Should fail with invalid password")
    void shouldFailWithInvalidPassword() {
        // Arrange
        final User user = new User("user", "u@t.com", "hash");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordService.verify(anyString(), any())).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> authService.login("user", "wrong"));
    }
}
