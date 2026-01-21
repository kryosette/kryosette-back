package com.example.demo.application.dtos.user;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for private user information.
 * Contains sensitive user details that should only be exposed to the user themselves
 * or authorized systems.
 *
 * <p>Fields included:
 * <ul>
 *   <li>First name</li>
 *   <li>Last name</li>
 *   <li>Email address</li>
 *   <li>User ID</li>
 *   <li>Username</li>
 * </ul>
 */
@Data
public class UserDtoPrivate {
    /**
     * User's legal first name
     */
    private String firstname;

    /**
     * User's legal last name
     */
    private String lastname;

    /**
     * User's primary email address (used for authentication)
     */
    private String email;

    /**
     * Unique system identifier for the user
     */
    private String userId;

    /**
     * Public display name/handle for the user
     */
    private String username;

    /**
     * Constructs a DTO with core user identifiers
     * @param userId Unique system ID
     * @param username Public display name
     * @param email Authentication email
     */
    public UserDtoPrivate(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}