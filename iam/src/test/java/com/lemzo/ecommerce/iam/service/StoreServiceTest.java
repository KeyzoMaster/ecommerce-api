package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.domain.Store;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.iam.repository.StoreRepository;
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
@DisplayName("StoreService Unit Tests")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("Should successfully create a store")
    void shouldCreateStoreSuccessfully() {
        // Arrange
        final User owner = new User("owner", "owner@test.com", "pass");
        final String slug = "my-store";
        
        when(storeRepository.findByName(slug)).thenReturn(Optional.empty());
        when(storeRepository.insert(any(Store.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        final Store result = storeService.createStore("My Store", slug, owner);

        // Assert
        assertNotNull(result);
        assertEquals("My Store", result.getName());
        verify(storeRepository).insert(any(Store.class));
    }

    @Test
    @DisplayName("Should throw exception if slug taken")
    void shouldThrowIfSlugTaken() {
        // Arrange
        final User owner = new User("owner", "owner@test.com", "pass");
        when(storeRepository.findByName(anyString())).thenReturn(Optional.of(new Store("Other", "slug", owner)));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> storeService.createStore("New", "slug", owner));
    }
}
