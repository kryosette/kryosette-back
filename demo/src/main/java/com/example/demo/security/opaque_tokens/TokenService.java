package com.example.demo.security.opaque_tokens;

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
public class TokenService {

    private final KryocacheClient kryocacheClient;
    private final ObjectMapper objectMapper;
    private static final int MAX_TOKENS_PER_DEVICE = 2; // Максимум 2 токена на deviceHash
    private static final long TOKEN_EXPIRATION_MS = 3600000L; // 1 час
    private static final long DEVICE_TOKEN_LIMIT_TTL = 86400L; // 24 часа в секундах

    public String generateToken(UserDetails userDetails, String userId, String deviceHash, String clientIp) {
        try {
            if (!checkDeviceTokenLimit(deviceHash)) {
                log.warn("Device token limit reached for deviceHash: {}. Max allowed: {}",
                        deviceHash, MAX_TOKENS_PER_DEVICE);
                throw new TokenGenerationException("Too many active tokens for this device");
            }

            String tokenId = UUID.randomUUID().toString();
            Instant now = Instant.now();
            Instant expiration = now.plusMillis(TOKEN_EXPIRATION_MS);

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
                    (int) (TOKEN_EXPIRATION_MS / 1000)
            );

            if (!success) {
                log.error("Failed to store token in Kryocache");
                throw new TokenGenerationException("Failed to generate token");
            }

            incrementDeviceTokenCounter(deviceHash);

            saveTokenToDeviceMapping(tokenId, deviceHash);

            if (deviceHash != null && !deviceHash.isEmpty()) {
                saveDeviceToTokenMapping(deviceHash, tokenId);
                saveDeviceToUserMapping(deviceHash, userDetails.getUsername());

                if (clientIp != null && !clientIp.isEmpty()) {
                    saveIpToDeviceMapping(clientIp, deviceHash);
                }
            }

            log.info("Token generated successfully. TokenId: {}, DeviceHash: {}, User: {}",
                    tokenId, deviceHash, userDetails.getUsername());
            return tokenId;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize token data", e);
            throw new TokenGenerationException("Failed to generate token");
        }
    }

    private boolean checkDeviceTokenLimit(String deviceHash) {
        if (deviceHash == null || deviceHash.isEmpty()) {
            return true; // Для анонимных устройств без deviceHash нет лимита
        }

        try {
            String counterKey = "device_token_counter:" + deviceHash;
            String counterStr = kryocacheClient.getToken(counterKey);

            if (counterStr == null || counterStr.isEmpty()) {
                return true; // Еще нет токенов для этого deviceHash
            }

            int currentCount = Integer.parseInt(counterStr);
            log.debug("Current token count for deviceHash {}: {}", deviceHash, currentCount);

            return currentCount < MAX_TOKENS_PER_DEVICE;
        } catch (Exception e) {
            log.error("Failed to check device token limit for deviceHash: {}", deviceHash, e);
            return true; // В случае ошибки разрешаем создание токена
        }
    }

    private void saveTokenToDeviceMapping(String tokenId, String deviceHash) {
        try {
            kryocacheClient.setToken(
                    "token_device:" + tokenId,
                    deviceHash,
                    (int) (TOKEN_EXPIRATION_MS / 1000) // TTL такой же как у токена
            );
            log.debug("Saved Token->Device mapping: {} -> {}", tokenId, deviceHash);
        } catch (Exception e) {
            log.error("Failed to save Token->Device mapping", e);
        }
    }

    /**
     * Увеличивает счетчик токенов для deviceHash
     */
    private void incrementDeviceTokenCounter(String deviceHash) {
        if (deviceHash == null || deviceHash.isEmpty()) {
            return;
        }

        try {
            String counterKey = "device_token_counter:" + deviceHash;
            String currentCounter = kryocacheClient.getToken(counterKey);

            int newCount = 1;
            if (currentCounter != null && !currentCounter.isEmpty()) {
                try {
                    newCount = Integer.parseInt(currentCounter) + 1;
                } catch (NumberFormatException e) {
                    log.warn("Invalid counter value for deviceHash {}: {}", deviceHash, currentCounter);
                    newCount = 1;
                }
            }

            // Устанавливаем TTL 24 часа, чтобы счетчик автоматически очищался
            kryocacheClient.setToken(
                    counterKey,
                    String.valueOf(newCount),
                    (int) DEVICE_TOKEN_LIMIT_TTL
            );

            log.debug("Incremented token counter for deviceHash {} to {}", deviceHash, newCount);
        } catch (Exception e) {
            log.error("Failed to increment device token counter for deviceHash: {}", deviceHash, e);
        }
    }

    /**
     * Уменьшает счетчик токенов для deviceHash
     */
    private void decrementDeviceTokenCounter(String deviceHash) {
        if (deviceHash == null || deviceHash.isEmpty()) {
            return;
        }

        try {
            String counterKey = "device_token_counter:" + deviceHash;
            String currentCounter = kryocacheClient.getToken(counterKey);

            if (currentCounter != null && !currentCounter.isEmpty()) {
                try {
                    int currentCount = Integer.parseInt(currentCounter);
                    int newCount = Math.max(0, currentCount - 1);

                    kryocacheClient.setToken(
                            counterKey,
                            String.valueOf(newCount),
                            (int) DEVICE_TOKEN_LIMIT_TTL
                    );

                    log.debug("Decremented token counter for deviceHash {} from {} to {}",
                            deviceHash, currentCount, newCount);

                    // Если счетчик достиг 0, удаляем ключ
                    if (newCount == 0) {
                        kryocacheClient.delete(counterKey);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid counter value for deviceHash {}: {}", deviceHash, currentCounter);
                    kryocacheClient.delete(counterKey);
                }
            }
        } catch (Exception e) {
            log.error("Failed to decrement device token counter for deviceHash: {}", deviceHash, e);
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
        log.info("DEBUG [getTokenData]: tokenId = '{}'", tokenId);

        if (!StringUtils.hasText(tokenId)) {
            log.warn("DEBUG [getTokenData]: tokenId is null or empty");
            return Optional.empty();
        }

        String key = "token:" + tokenId;
        log.info("DEBUG [getTokenData]: Looking for key '{}' in Kryocache", key);

        String data = kryocacheClient.getToken(key);
        log.info("DEBUG [getTokenData]: Kryocache response for key '{}': '{}'", key, data);

        if (data == null) {
            log.warn("DEBUG [getTokenData]: Token data not found in Kryocache");
            return Optional.empty();
        }

        try {
            TokenData tokenData = objectMapper.readValue(data, TokenData.class);
            log.info("DEBUG [getTokenData]: Successfully parsed token data for user: {}",
                    tokenData.getUsername());
            return Optional.of(tokenData);
        } catch (JsonProcessingException e) {
            log.error("DEBUG [getTokenData]: Failed to parse token data: {}", data, e);
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

    public void saveDeviceToTokenMapping(String deviceHash, String tokenId) {
        try {
            kryocacheClient.setToken(
                    "device_token:" + deviceHash,
                    tokenId,
                    (int) Duration.ofHours(24).toSeconds()
            );
            log.debug("Saved Device->Token mapping: {} -> {}", deviceHash, tokenId);
        } catch (Exception e) {
            log.error("Failed to save Device->Token mapping", e);
        }
    }

    public String findTokenByDeviceHash(String deviceHash) {
        try {
            // Сначала получаем token_id по device_hash
            String tokenId = kryocacheClient.getToken("device_token:" + deviceHash);
            if (tokenId == null || tokenId.isEmpty()) {
                log.warn("No token found for device hash: {}", deviceHash);
                return null;
            }

            // Затем получаем сам токен по token_id
            String tokenData = kryocacheClient.getToken("token:" + tokenId);
            if (tokenData == null) {
                log.warn("Token data not found for tokenId: {}", tokenId);
                return null;
            }

            return tokenData;
        } catch (Exception e) {
            log.error("Failed to find token by device hash: {}", deviceHash, e);
            return null;
        }
    }
}