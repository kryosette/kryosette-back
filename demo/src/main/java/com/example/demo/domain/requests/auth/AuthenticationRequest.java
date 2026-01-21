package com.example.demo.domain.requests.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an authentication request containing user credentials.
 * Used as a request body for login/authentication endpoints.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Email: Must be non-null, non-empty, and properly formatted</li>
 *   <li>Password: Must be non-null, non-empty, and at least 8 characters</li>
 * </ul>
 *
 * @see javax.validation.constraints For validation annotations
 * @see lombok.Builder For fluent construction
 */
@Getter
@Setter
@Builder
public class AuthenticationRequest {

    @Email(message = "Email is not well formatted")
    @NotEmpty(message = "Email is mandatory")
    @NotNull(message = "Email is mandatory")
    private String email;

    /**
     * User's plaintext password.
     * Must satisfy all conditions:
     * <ol>
     *   <li>Minimum 8 characters length</li>
     *   <li>Non-empty string</li>
     *   <li>Non-null value</li>
     * </ol>
     *
     * @implNote Actual password complexity rules (special chars, numbers etc.)
     *           should be enforced by {@link PasswordValidationService}
     * @apiNote Backed by Java Bean Validation:
     * <ul>
     *   <li>{@link Size} for length check</li>
     *   <li>{@link NotEmpty} for content check</li>
     *   <li>{@link NotNull} for null check</li>
     * </ul>
     */
    @NotEmpty(message = "Password is mandatory")
    @NotNull(message = "Password is mandatory")
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    private String password;
}