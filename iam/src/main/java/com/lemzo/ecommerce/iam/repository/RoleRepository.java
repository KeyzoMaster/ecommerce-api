package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.iam.domain.Role;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository {

    @Insert
    Role insert(Role role);

    @Save
    Role save(Role role);

    @Find
    Optional<Role> findById(UUID id);

    @Find
    Optional<Role> findByName(String name);

    @Find
    List<Role> findAll();

    @Delete
    void delete(Role role);
}
