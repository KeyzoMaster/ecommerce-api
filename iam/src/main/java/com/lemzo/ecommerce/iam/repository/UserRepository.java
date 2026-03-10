package com.lemzo.ecommerce.iam.repository;

import com.lemzo.ecommerce.iam.domain.User;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les utilisateurs.
 */
@Repository
public interface UserRepository {

    @Insert
    User insert(User user);

    @Update
    User update(User user);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.addresses WHERE u.id = :id")
    Optional<User> findById(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) FROM User u")
    long count();
}
