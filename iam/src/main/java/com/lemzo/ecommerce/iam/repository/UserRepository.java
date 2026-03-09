package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.iam.domain.User;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import jakarta.data.repository.Param;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les utilisateurs utilisant Jakarta Data 1.0.
 */
@Repository
public interface UserRepository {

    @Insert
    User insert(User user);

    @Update
    User update(User user);

    @Find
    Optional<User> findById(UUID id);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Find
    Page<User> findAll(PageRequest pageRequest);

    @Delete
    void delete(User user);
}
