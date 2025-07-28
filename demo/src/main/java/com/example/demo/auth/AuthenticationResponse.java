package com.example.demo.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

/**
 * Represents the response payload for successful authentication operations.
 * Contains the opaque token used for subsequent authenticated requests.
 *
 * <p>This is typically returned by:
 * <ul>
 *   <li>Login endpoints</li>
 *   <li>Token refresh endpoints</li>
 *   <li>Re-authentication flows</li>
 * </ul>
 *
 * @see lombok.Builder For fluent construction pattern
 * @see lombok.Getter/@Setter For automatic property accessors
 */
@Getter
@Setter
@Builder
public class AuthenticationResponse {

    /**
     * Opaque bearer token string used for authentication.
     * This is not a JWT but a reference token (UUID format) that:
     * <ul>
     *   <li>References stored session data in Redis</li>
     *   <li>Has server-defined expiration</li>
     *   <li>Must be included in Authorization header as "Bearer {token}"</li>
     * </ul>
     *
     * @apiNote Security characteristics:
     * <ol>
     *   <li>Non-guessable (cryptographically random UUID)</li>
     *   <li>Server-side stateful (revocable)</li>
     *   <li>Transport must use HTTPS</li>
     * </ol>
     *
     * @implNote The actual token data is stored in Redis via {@link TokenService},
     *           containing user claims and metadata.
     */
    private String token;
}