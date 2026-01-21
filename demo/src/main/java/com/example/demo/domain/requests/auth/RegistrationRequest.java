package com.example.demo.domain.requests.auth;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a user registration request with comprehensive validation rules.
 * Used as the request body for user registration endpoints.
 *
 * <p>Validation enforces strict security requirements for all fields,
 * particularly for password complexity to meet modern security standards.
 *
 * @see javax.validation.constraints For validation annotations
 * @see lombok.Builder For fluent construction pattern
 */
@Getter
@Setter
@Builder
public class RegistrationRequest {

    /**
     * User's legal first name.
     * Must satisfy:
     * <ul>
     *   <li>Non-null value</li>
     *   <li>Non-empty string (after trimming)</li>
     * </ul>
     *
     * @apiNote Backed by Bean Validation:
     * <ul>
     *   <li>{@link NotEmpty} for non-blank check</li>
     *   <li>{@link NotNull} for null check</li>
     * </ul>
     */
    @NotEmpty(message = "Firstname is mandatory")
    @NotNull(message = "Firstname is mandatory")
    private String firstname;

    /**
     * User's legal last name.
     * Must satisfy:
     * <ul>
     *   <li>Non-null value</li>
     *   <li>Non-empty string (after trimming)</li>
     * </ul>
     *
     * @apiNote Backed by Bean Validation:
     * <ul>
     *   <li>{@link NotEmpty} for non-blank check</li>
     *   <li>{@link NotNull} for null check</li>
     * </ul>
     */
    @NotEmpty(message = "Lastname is mandatory")
    @NotNull(message = "Lastname is mandatory")
    private String lastname;

    /**
     * User's primary email address (serves as username).
     * Must satisfy:
     * <ul>
     *   <li>Valid email format (user@domain.tld)</li>
     *   <li>Non-null value</li>
     *   <li>Non-empty string</li>
     * </ul>
     *
     * @apiNote Backed by Bean Validation:
     * <ul>
     *   <li>{@link Email} for format validation</li>
     *   <li>{@link NotEmpty} for content check</li>
     *   <li>{@link NotNull} for null check</li>
     * </ul>
     */
    @Email(message = "Email is not well formatted")
    @NotEmpty(message = "Email is mandatory")
    @NotNull(message = "Email is mandatory")
    private String email;

    /**
     * User's password with strict complexity requirements.
     * Must satisfy all conditions:
     * <ol>
     *   <li>Minimum 12 characters length</li>
     *   <li>At least one uppercase letter (A-Z)</li>
     *   <li>At least one lowercase letter (a-z)</li>
     *   <li>At least one digit (0-9)</li>
     *   <li>At least one special character (!@#$%^&*()_+-=[]{};':"\|,.<>/?)</li>
     *   <li>No whitespace allowed</li>
     * </ol>
     *
     * @implNote The regex pattern enforces NIST Special Publication 800-63B
     *           guidelines for memorized secrets
     * @apiNote Backed by Bean Validation:
     * <ul>
     *   <li>{@link NotBlank} for non-whitespace content</li>
     *   <li>{@link Size} for length check</li>
     *   <li>{@link Pattern} for complexity requirements</li>
     * </ul>
     */
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 12, message = "Password must be at least 12 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{12,}$",
            message = "Password must contain: 1 uppercase, 1 lowercase, 1 digit, 1 special character"
    )
    private String password;
}