package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.iam.api.dto.RoleCreateRequest;
import com.lemzo.ecommerce.iam.domain.Permission;
import com.lemzo.ecommerce.iam.domain.Role;
import com.lemzo.ecommerce.iam.repository.PermissionRepository;
import com.lemzo.ecommerce.iam.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleManagementService Unit Tests")
class RoleManagementServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleManagementService roleService;

    @Test
    void shouldCreateRole() {
        final RoleCreateRequest request = new RoleCreateRequest("TEST_ROLE", "Test Description", Set.of("product:read"));
        final Permission permission = new Permission(ResourceType.PRODUCT, PbacAction.READ);
        
        when(roleRepository.findByName("TEST_ROLE")).thenReturn(Optional.empty());
        when(permissionRepository.findAll()).thenReturn(Stream.of(permission));
        when(roleRepository.insert(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Role result = roleService.createRole(request);

        assertNotNull(result);
        assertEquals("TEST_ROLE", result.getName());
        verify(roleRepository).insert(any(Role.class));
    }

    @Test
    void shouldThrowWhenRoleExists() {
        // Arrange
        final RoleCreateRequest request = new RoleCreateRequest("ADMIN", "desc", Set.of());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN", "desc")));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> roleService.createRole(request));
    }
}
