package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.repository.UserRepository;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should successfully register a user")
    void shouldRegisterSuccessfully() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordService.hash(any())).thenReturn("hashed");
        when(userRepository.insert(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        final User result = userService.register("testuser", "test@ecommerce.local", "password");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).insert(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception if username taken")
    void shouldThrowIfUsernameTaken() {
        when(userRepository.findByUsername("taken")).thenReturn(Optional.of(new User("taken", "e", "p")));
        assertThrows(BusinessRuleException.class, () -> userService.register("taken", "other", "p"));
    }
}
