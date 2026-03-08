package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.iam.domain.Permission;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository {

    @Insert
    Permission insert(Permission permission);

    @Find
    Optional<Permission> findById(UUID id);

    @Find
    Optional<Permission> findByResourceTypeAndAction(ResourceType resourceType, PbacAction action);

    @Find
    List<Permission> findAll();
}
