package com.example.demo.domain.services.opaque_tokens;

import com.example.demo.domain.model.opaque_tokens.TokenData;
import com.example.demo.domain.model.opaque_tokens.TokenDataDev;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceDev {

    private final KryocacheClient kryocacheClient;
    private final ObjectMapper objectMapper;

    public String generateToken(UserDetails userDetails, String userId, String deviceHash, String clientIp) {
        try {
            String tokenId = UUID.randomUUID().toString();
            Instant now = Instant.now();
            Instant expiration = now.plusMillis(getTokenExpiration());

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

            boolean success = kryocacheClient.setToken(
                    "token:" + tokenId,
                    objectMapper.writeValueAsString(tokenData),
                    (int) (getTokenExpiration() / 1000)  // Конвертируем в секунды
            );

            if (!success) {
                log.error("Failed to store token in Kryocache");
                throw new TokenGenerationException("Failed to generate token");
            }

            if (clientIp != null && !clientIp.isEmpty() && deviceHash != null) {
                saveIpToDeviceMapping(clientIp, deviceHash);
                saveDeviceToUserMapping(deviceHash, userDetails.getUsername());
            }

            return tokenId;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize token data", e);
            throw new TokenGenerationException("Failed to generate token");
        }
    }

    /**
     * Get token expiration from properties (default 3600000ms = 1 hour)
     */
    private long getTokenExpiration() {
        // Можно добавить @Value injection если нужно
        return 3600000L; // 1 hour in milliseconds
    }

    public void saveIpToDeviceMapping(String clientIp, String deviceHash) {
        try {
            // IP -> device hash (TTL 24 часа)
            kryocacheClient.setToken(
                    "ip_device:" + clientIp,
                    deviceHash,
                    (int) Duration.ofHours(24).toSeconds()
            );

            // Device -> IP для обратного поиска
            kryocacheClient.setToken(
                    "device_ip:" + deviceHash,
                    clientIp,
                    (int) Duration.ofHours(24).toSeconds()
            );

            log.debug("Saved IP->Device mapping: {} -> {}", clientIp, deviceHash);
        } catch (Exception e) {
            log.error("Failed to save IP->Device mapping", e);
        }
    }

    public String findDeviceHashByIp(String ip) {
        try {
            return kryocacheClient.getToken("ip_device:" + ip);
        } catch (Exception e) {
            log.error("Failed to find device hash by IP: {}", ip, e);
            return null;
        }
    }

    /**
     * Ищем username по device hash
     */
    public String findUsernameByDeviceHash(String deviceHash) {
        try {
            String username = kryocacheClient.getToken("device_user:" + deviceHash);
            if (username != null) {
                return username;
            }

            log.warn("No username found for device hash: {}", deviceHash);
            return null;
        } catch (Exception e) {
            log.error("Failed to find username by device hash: {}", deviceHash, e);
            return null;
        }
    }

    /**
     * Сохраняем маппинг device hash -> username
     */
    public void saveDeviceToUserMapping(String deviceHash, String username) {
        try {
            kryocacheClient.setToken(
                    "device_user:" + deviceHash,
                    username,
                    (int) Duration.ofHours(24).toSeconds()
            );
            log.debug("Saved Device->User mapping: {} -> {}", deviceHash, username);
        } catch (Exception e) {
            log.error("Failed to save Device->User mapping", e);
        }
    }

//    public String findIpByDeviceHash(String deviceHash) {
//        try {
//            return kryocacheClient.getToken("device_ip:" + deviceHash);
//        } catch (Exception e) {
//            log.error("Failed to find IP by device hash: {}", deviceHash, e);
//            return null;
//        }
//    }

    /**
     * Validates a token's existence and expiration.
     */
    public boolean isTokenValid(String tokenId) {
        if (!StringUtils.hasText(tokenId)) {
            return false;
        }

        String data = kryocacheClient.getToken("token:" + tokenId);
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

    public Optional<TokenData> getTokenData(String tokenId) {
        if (!StringUtils.hasText(tokenId)) {
            return Optional.empty();
        }

        String data = kryocacheClient.getToken("token:" + tokenId);
        if (data == null) {
            return Optional.empty();
        }

        try {
            TokenData tokenData = objectMapper.readValue(data, TokenData.class);
            return Optional.of(tokenData);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse token data", e);
            return Optional.empty();
        }
    }

    /**
     * Gets a filtered JSON representation of token data.
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
     * Invalidates a token by removing it from Kryocache.
     */
    public void invalidateToken(String tokenId) {
        if (StringUtils.hasText(tokenId)) {
            kryocacheClient.delete("token:" + tokenId);
        }
    }

    /**
     * Generates a Transaction Authentication Number (TAN) for time-sensitive operations.
     */
    public String generateTan(String userId, String operationId) {
        String tan = UUID.randomUUID().toString();
        kryocacheClient.setToken(
                "tan:" + tan,
                userId + ":" + operationId,
                (int) TimeUnit.MINUTES.toSeconds(5)
        );
        return tan;
    }

    /**
     * Verifies and consumes a TAN.
     */
    public Optional<Pair<String, String>> verifyTan(String tan) {
        if (!StringUtils.hasText(tan)) {
            return Optional.empty();
        }

        String key = "tan:" + tan;
        String value = kryocacheClient.getToken(key);
        if (value == null) {
            return Optional.empty();
        }

        kryocacheClient.delete(key);

        String[] parts = value.split(":");
        if (parts.length != 2) {
            return Optional.empty();
        }

        return Optional.of(Pair.of(parts[0], parts[1]));
    }

    /**
     * Для обратной совместимости с TokenController
     */
    public String createToken(String userId, String userData, int ttlSeconds) {
        // Создаем минимальный UserDetails
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                userId,
                "",
                List.of(() -> "ROLE_USER")
        );

        return generateToken(
                userDetails,
                userId,
                "legacy-device-hash",
                "127.0.0.1"
        );
    }

    /**
     * Для обратной совместимости с TokenController
     */
    public boolean validateToken(String token) {
        return isTokenValid(token);
    }

    /**
     * Для обратной совместимости с TokenController
     */
    public TokenDataDev getTokenDataOld(String token) {
        Optional<TokenData> tokenData = getTokenData(token);
        if (tokenData.isEmpty()) {
            return null;
        }

        TokenData td = tokenData.get();
        return TokenDataDev.builder()
                .tokenId(td.getTokenId())
                .userId(td.getUserId())
                .username(td.getUsername())
                .authorities(td.getAuthorities())
                .deviceHash(td.getDeviceHash())
                .issuedAt(td.getIssuedAt().toEpochMilli())
                .expiresAt(td.getExpiresAt().toEpochMilli())
                .build();
    }

    /**
     * Для обратной совместимости с TokenController
     */
    public boolean revokeToken(String token) {
        invalidateToken(token);
        return true;
    }

    /**
     * Для обратной совместимости с TokenController
     */
    public boolean refreshToken(String token, int ttlSeconds) {
        Optional<TokenData> tokenDataOpt = getTokenData(token);
        if (tokenDataOpt.isEmpty()) {
            return false;
        }

        TokenData tokenData = tokenDataOpt.get();
        Instant newExpiration = Instant.now().plusSeconds(ttlSeconds);

        // Обновляем время истечения
        TokenData updatedTokenData = new TokenData(
                tokenData.getTokenId(),
                tokenData.getUserId(),
                tokenData.getUsername(),
                tokenData.getAuthorities(),
                tokenData.getDeviceHash(),
                tokenData.getIssuedAt(),
                newExpiration
        );

        try {
            boolean success = kryocacheClient.setToken(
                    "token:" + token,
                    objectMapper.writeValueAsString(updatedTokenData),
                    ttlSeconds
            );

            return success;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize token data for refresh", e);
            return false;
        }
    }

    /**
     * Thrown when token generation fails.
     */
    public static final class TokenGenerationException extends RuntimeException {
        public TokenGenerationException(String message) {
            super(message);
        }
    }
}