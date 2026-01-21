package com.example.demo.application.dtos.user;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for public user information.
 * Contains non-sensitive user details that can be safely exposed to other users.
 *
 * <p>Fields included:
 * <ul>
 *   <li>First name</li>
 *   <li>Last name</li>
 *   <li>Email address</li>
 *   <li>User ID</li>
 *   <li>Username</li>
 * </ul>
 *
 * <p>Note: Unlike {@link UserDtoPrivate}, this DTO is designed for public exposure
 * and may contain fewer fields or different field visibility in practice.
 */
@Data
public class UserDto {
    /**
     * User's legal first name (may be hidden in some contexts)
     */
    private String firstname;

    /**
     * User's legal last name (may be hidden in some contexts)
     */
    private String lastname;

    /**
     * User's email address (may be hidden in some contexts)
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
     * Constructs a DTO with basic public user information
     * @param username Public display name
     * @param email User's email address (may be visible to certain roles)
     */
    public UserDto(String username, String email) {
        this.username = username;
        this.email = email;
    }
}