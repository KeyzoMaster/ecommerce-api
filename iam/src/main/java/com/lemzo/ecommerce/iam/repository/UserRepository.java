package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.iam.domain.User;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les utilisateurs utilisant Jakarta Data 1.0.
 */
@Repository
public interface UserRepository extends BasicRepository<User, UUID> {

    @Find
    Optional<User> findByUsername(String username);

    @Find
    Optional<User> findByEmail(String email);

    @Find
    Page<User> findAll(PageRequest pageRequest);
}
