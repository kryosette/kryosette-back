package com.example.demo.auth;

import com.example.demo.security.opaque_tokens.TokenData;
import com.example.demo.security.opaque_tokens.TokenService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import com.example.demo.auth.password.PasswordValidationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for authentication and user management operations.
 * Handles registration, authentication, token verification, and admin actions.
 *
 * <p>Security notes:
 * <ul>
 *   <li>Uses opaque tokens (non-JWT) stored in Redis via {@link TokenService}</li>
 *   <li>Password validation occurs before authentication</li>
 *   <li>Sensitive operations require ADMIN authority</li>
 * </ul>
 *
 * @see TokenService For token management
 * @see AuthenticationService For core auth logic
 */
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {

    /**
     * Service for generating and validating opaque tokens.
     */
    private final TokenService tokenService;

    /**
     * Core authentication service.
     */
    private final AuthenticationService service;

    /**
     * Service for password strength validation.
     */
    private final PasswordValidationService passwordValidationService;

    /**
     * Repository for user data access.
     */
    private final UserRepository userRepository;

    /**
     * Hardcoded address for the Node.js authentication server.
     * @implNote Should be externalized to configuration in production.
     */
    private static final String NODE_SERVER_ADDRESS = "127.0.0.1";

    /**
     * Hardcoded port for the Node.js authentication server.
     * @implNote Should be externalized to configuration in production.
     */
    private static final int NODE_SERVER_PORT = 8081;

    /**
     * Registers a new user account.
     *
     * @param request Validated registration request containing:
     *                <ul>
     *                  <li>email</li>
     *                  <li>password</li>
     *                  <li>other user details</li>
     *                </ul>
     * @return HTTP 202 (Accepted) on success
     * @throws MessagingException If account activation email fails to send
     * @see RegistrationRequest For request structure
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        service.register(request);
        return ResponseEntity.accepted().build();
    }

    /**
     * Authenticates a user and generates an opaque token.
     *
     * @param request Validated authentication request containing email and password
     * @param httpRequest Raw HTTP request for context (e.g., IP/device tracking)
     * @return {@link AuthenticationResponse} containing:
     *         <ul>
     *           <li>Opaque token ID</li>
     *           <li>User metadata</li>
     *         </ul>
     * @throws Exception If:
     *         <ul>
     *           <li>Password validation fails</li>
     *           <li>Node.js server communication fails</li>
     *           <li>Encryption fails</li>
     *         </ul>
     * @implNote Flow:
     * <ol>
     *   <li>Validates password strength</li>
     *   <li>Authenticates via {@link AuthenticationService}</li>
     *   <li>Encrypts password and verifies with Node.js server</li>
     * </ol>
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest
    ) throws Exception {
        passwordValidationService.validatePassword(request.getPassword());
        AuthenticationResponse response = service.authenticate(request, httpRequest);
        String encryptedPassword = encryptPassword(request.getPassword());

        try (Socket socket = new Socket(NODE_SERVER_ADDRESS, NODE_SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            JSONObject json = new JSONObject();
            json.put("password", encryptedPassword);
            out.println(json.toString());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ERROR: Could not communicate with C server");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Encrypts a password using AES-GCM-256.
     *
     * @param password Plaintext password to encrypt
     * @return Base64-encoded string containing:
     *         <ul>
     *           <li>12-byte IV</li>
     *           <li>Encrypted password</li>
     *         </ul>
     * @throws Exception If encryption fails
     * @implNote Security parameters:
     * <ul>
     *   <li>Key: 256-bit AES</li>
     *   <li>Mode: GCM with 128-bit auth tag</li>
     *   <li>IV: 12-byte cryptographically secure random</li>
     * </ul>
     */
    private String encryptPassword(String password) throws Exception {
        String secretKey = "YourSuperSecretKeyWith32Characters!!";
        byte[] keyBytes = normalizeKey(secretKey, 32);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec ivSpec = new GCMParameterSpec(128, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Normalizes a key to the required size by truncating or zero-padding.
     *
     * @param key Original key string
     * @param requiredSize Desired key length in bytes
     * @return Normalized key bytes
     */
    private byte[] normalizeKey(String key, int requiredSize) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] normalizedKey = new byte[requiredSize];
        System.arraycopy(
                keyBytes,
                0,
                normalizedKey,
                0,
                Math.min(keyBytes.length, requiredSize)
        );
        return normalizedKey;
    }

    /**
     * Activates a user account using a registration token.
     *
     * @param token Opaque activation token
     * @throws MessagingException If activation fails
     */
    @GetMapping("/activate-account")
    public void confirm(
            @RequestParam String token
    ) throws MessagingException {
        service.activateAccount(token);
    }

    /**
     * Verifies an opaque token's validity.
     *
     * @param authHeader "Bearer {token}" format header
     * @return HTTP 200 with user details if valid, HTTP 401 otherwise
     * @implNote Responses:
     * <ul>
     *   <li>200: { "username": "...", "userId": "..." }</li>
     *   <li>401: { "error": "...", "message": "..." }</li>
     * </ul>
     * @apiNote Cached for performance (see {@code authCache} configuration)
     */
    @PostMapping("/verify")
    @Cacheable(value = "authCache",
            key = "T(org.apache.commons.lang3.StringUtils).substring(#authHeader, 7)")
    public ResponseEntity<?> verifyToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid authorization header",
                    "message", "Authorization header must start with 'Bearer '"
            ));
        }

        String token = authHeader.substring(7);
        Optional<TokenData> tokenData = tokenService.getTokenData(token);

        if (tokenData.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid token",
                    "message", "Token not found or expired"
            ));
        }

        if (tokenData.get().getExpiresAt().isBefore(java.time.Instant.now())) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Expired token",
                    "message", "Token has expired"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "username", tokenData.get().getUsername(),
                "userId", tokenData.get().getUserId()
        ));
    }

    /**
     * Locks a user account (admin-only).
     *
     * @param email User email to lock
     * @throws UsernameNotFoundException If user doesn't exist
     * @apiNote Requires ADMIN authority
     */
    @PostMapping("/lock-user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void lockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAccountLocked(true);
        userRepository.save(user);
    }

    /**
     * Unlocks a user account (admin-only).
     *
     * @param email User email to unlock
     * @return HTTP 200 on success
     * @apiNote Requires ADMIN authority
     */
    @PostMapping("/lock-user/{email}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> unlockUser(@PathVariable String email) {
        service.unlockUser(email);
        return ResponseEntity.ok().build();
    }

    /**
     * Registers a new admin account (admin-only).
     *
     * @param request Validated registration request
     * @return HTTP 202 (Accepted) on success
     * @throws MessagingException If activation email fails
     * @apiNote Requires ADMIN authority
     */
    @PostMapping("/register-admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> registerAdmin(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        service.registerAdmin(request);
        return ResponseEntity.accepted().build();
    }
}