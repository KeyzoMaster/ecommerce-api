package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.iam.domain.User;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour la gestion des utilisateurs (Jakarta Data).
 */
@Repository
public interface UserRepository {

    @Insert
    User insert(User user);

    @Save
    User save(User user);

    @Find
    Optional<User> findById(UUID id);

    @Find
    Optional<User> findByEmail(String email);

    @Find
    Optional<User> findByUsername(String username);

    @Find
    boolean existsByEmail(String email);

    @Find
    boolean existsByUsername(String username);
}
