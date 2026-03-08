package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.repository.UserRepository;
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
@DisplayName("UserService Unit Tests (JUnit 6)")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should successfully create a user")
    void shouldCreateUserSuccessfully() {
        // Arrange
        String username = "jdoe";
        String email = "jdoe@test.com";
        String password = "password123";
        String hashed = "hashed_pass";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordService.hash(any())).thenReturn(hashed);
        when(userRepository.insert(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        User result = userService.createUser(username, email, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(hashed, result.getPassword());
        verify(userRepository).insert(any(User.class));
    }

    @Test
    @DisplayName("Should fail if email already exists")
    void shouldFailIfEmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> 
            userService.createUser("user", "existing@test.com", "pass")
        );
    }

    @Test
    @DisplayName("Should find user by identifier")
    void shouldFindUserByIdentifier() {
        String identifier = "jdoe";
        User user = new User();
        user.setUsername(identifier);

        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByIdentifier(identifier);

        assertTrue(result.isPresent());
        assertEquals(identifier, result.get().getUsername());
    }
}
