package com.example.demo.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.webjars.NotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link User} entity operations.
 * Provides both standard CRUD operations and custom queries for user management.
 */
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Finds a user by email address with roles eagerly loaded.
     * Uses EntityGraph to optimize role loading in a single query.
     *
     * @param email Email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    // Additional commented query examples:
    // @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    // List<User> findAllWithRoles();

    // @EntityGraph(attributePaths = {"roles", "notifications"})
    // @Query("SELECT u FROM User u WHERE u.isOnline = true")
    // List<User> findOnlineUsersWithAssociations();

    // @Query("SELECT u.firstname FROM User u")
    // Page<String> findAllUsernames(Pageable pageable);

    // @EntityGraph(attributePaths = "roles")
    // Page<User> findAll(Pageable pageable);

    // @Query("SELECT u FROM User u LEFT JOIN FETCH u.friends WHERE u.id = :userId")
    // Optional<User> findByIdWithFriends(@Param("userId") String userId);

    // @Query("SELECT u FROM User u LEFT JOIN FETCH u.notifications WHERE u.id = :userId")
    // Optional<User> findByIdWithNotifications(@Param("userId") String userId);
}