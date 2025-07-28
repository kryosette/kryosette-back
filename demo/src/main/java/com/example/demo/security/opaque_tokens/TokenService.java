package com.example.demo.security.opaque_tokens;

import com.example.demo.user.UserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for managing token generation, validation, and invalidation.
 * Handles opaque tokens (non-JWT) stored in Redis with TTL-based expiration.
 * Also supports TAN (Transaction Authentication Number) generation/verification.
 *
 * <p>Tokens contain:
 * <ul>
 *   <li>Unique ID (UUID)</li>
 *   <li>User metadata (ID, username, authorities)</li>
 *   <li>Device fingerprint</li>
 *   <li>Issuance/expiration timestamps</li>
 * </ul>
 *
 * @see StringRedisTemplate For Redis operations
 * @see ObjectMapper For JSON serialization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    /**
     * Redis client for token storage. Uses String serialization for JSON compatibility.
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * JSON serializer/deserializer for token metadata.
     */
    private final ObjectMapper objectMapper;

    /**
     * Token expiration time in milliseconds. Injected from Spring properties.
     */
    @Value("${spring.security.token.expiration}")
    private long tokenExpiration;

    /**
     * Token issuer identifier. Injected from Spring properties.
     */
    @Value("${spring.security.token.issuer}")
    private String issuer;

    /**
     * Generates a new opaque token and stores it in Redis.
     *
     * @param userDetails Spring Security user details (username, authorities)
     * @param userId Unique user identifier (business-level ID)
     * @param deviceHash Cryptographic hash of the device fingerprint
     * @return Generated token ID (UUID)
     * @throws TokenGenerationException If JSON serialization fails
     * @implNote Token structure:
     * <pre>{@code
     * {
     *   "id": "uuid",
     *   "userId": "user123",
     *   "username": "email@example.com",
     *   "authorities": ["ROLE_USER"],
     *   "deviceHash": "sha256-hash",
     *   "issuedAt": "2023-01-01T00:00:00Z",
     *   "expiresAt": "2023-01-01T01:00:00Z"
     * }
     * }</pre>
     */
    public String generateToken(UserDetails userDetails, String userId, String deviceHash) {
        try {
            String tokenId = UUID.randomUUID().toString();
            Instant now = Instant.now();
            Instant expiration = now.plusMillis(tokenExpiration);

            TokenData tokenData = new TokenData(
                    tokenId,
                    userId,
                    userDetails.getUsername(),
                    userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()),
                    deviceHash,
                    now,
                    expiration
            );

            redisTemplate.opsForValue().set(
                    "token:" + tokenId,
                    objectMapper.writeValueAsString(tokenData),
                    tokenExpiration,
                    TimeUnit.MILLISECONDS
            );

            return tokenId;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize token data", e);
            throw new TokenGenerationException("Failed to generate token");
        }
    }

    /**
     * Validates a token's existence and expiration.
     *
     * @param tokenId Token ID to validate
     * @return {@code true} if the token exists in Redis and is not expired,
     *         {@code false} otherwise (including parse failures)
     */
    public boolean isTokenValid(String tokenId) {
        if (!StringUtils.hasText(tokenId)) {
            return false;
        }

        String data = redisTemplate.opsForValue().get("token:" + tokenId);
        if (data == null) {
            return false;
        }

        try {
            TokenData tokenData = objectMapper.readValue(data, TokenData.class);
            return tokenData.getExpiresAt().isAfter(Instant.now());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse token data", e);
            return false;
        }
    }

    /**
     * Retrieves deserialized token data if valid.
     *
     * @param tokenId Token ID to lookup
     * @return {@link Optional} containing {@link TokenData} if valid,
     *         empty otherwise
     */
    public Optional<TokenData> getTokenData(String tokenId) {
        if (!isTokenValid(tokenId)) {
            return Optional.empty();
        }

        try {
            String data = redisTemplate.opsForValue().get("token:" + tokenId);
            if (data == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(data, TokenData.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse token data", e);
            return Optional.empty();
        }
    }

    /**
     * Gets a filtered JSON representation of token data.
     *
     * @param tokenId Token ID to serialize
     * @return JSON string containing username, userId, roles, and device hash,
     *         or empty if token is invalid
     * @implNote Output format:
     * <pre>{@code
     * {
     *   "username": "email@example.com",
     *   "userId": "user123",
     *   "roles": ["ROLE_USER"],
     *   "device": "sha256-hash"
     * }
     * }</pre>
     */
    public Optional<String> getTokenJsonData(String tokenId) {
        return getTokenData(tokenId).map(tokenData -> {
            try {
                return objectMapper.writeValueAsString(
                        Map.of(
                                "username", tokenData.getUsername(),
                                "userId", tokenData.getUserId(),
                                "roles", tokenData.getAuthorities(),
                                "device", tokenData.getDeviceHash()
                        )
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize token data", e);
            }
        });
    }

    /**
     * Invalidates a token by removing it from Redis.
     *
     * @param tokenId Token ID to invalidate
     */
    public void invalidateToken(String tokenId) {
        if (StringUtils.hasText(tokenId)) {
            redisTemplate.delete("token:" + tokenId);
        }
    }

    /**
     * Generates a Transaction Authentication Number (TAN) for time-sensitive operations.
     *
     * @param userId User ID to associate with the TAN
     * @param operationId Operation identifier (e.g., "password-reset")
     * @return Generated TAN (UUID format)
     * @implNote TANs are stored in Redis with a 5-minute TTL
     */
    public String generateTan(String userId, String operationId) {
        String tan = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "tan:" + tan,
                userId + ":" + operationId,
                5, TimeUnit.MINUTES
        );
        return tan;
    }

    /**
     * Verifies and consumes a TAN.
     *
     * @param tan TAN to verify
     * @return {@link Optional} containing a {@link Pair} of (userId, operationId) if valid,
     *         empty otherwise
     * @implNote This method is atomic - successful verification deletes the TAN
     */
    public Optional<Pair<String, String>> verifyTan(String tan) {
        if (!StringUtils.hasText(tan)) {
            return Optional.empty();
        }

        String key = "tan:" + tan;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }

        redisTemplate.delete(key);

        String[] parts = value.split(":");
        if (parts.length != 2) {
            return Optional.empty();
        }

        return Optional.of(Pair.of(parts[0], parts[1]));
    }

    /**
     * Thrown when token generation fails due to serialization errors.
     */
    public static final class TokenGenerationException extends RuntimeException {
        /**
         * @param message Human-readable error description
         */
        public TokenGenerationException(String message) {
            super(message);
        }
    }
}