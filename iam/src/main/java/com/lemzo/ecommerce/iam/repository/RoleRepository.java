package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.iam.domain.Role;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Repository pour les rôles.
 */
@Repository
public interface RoleRepository {

    @Insert
    Role insert(Role role);

    @Update
    Role update(Role role);

    @Find
    Optional<Role> findById(UUID id);

    @Find
    Optional<Role> findByName(String name);

    @Find
    Stream<Role> findAll();

    @Find
    List<Role> findAllList();

    @Delete
    void delete(Role role);
}
