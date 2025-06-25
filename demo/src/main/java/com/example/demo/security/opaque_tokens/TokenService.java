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

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.token.expiration}")
    private long tokenExpiration;

    @Value("${spring.security.token.issuer}")
    private String issuer;

    /**
     * Генерация нового opaque token
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

            // Сохраняем данные токена в Redis
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
     * Проверка валидности токена
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
     * Получение данных токена
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

    public Optional<String> getTokenJsonData(String tokenId) {
        return getTokenData(tokenId).map(tokenData -> {
            try {
                return objectMapper.writeValueAsString(
                        Map.of(
                                "username", tokenData.getUsername(),
                                "userId", tokenData.getUserId(),
                                "roles", tokenData.getAuthorities()
                        )
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize token data", e);
            }
        });
    }
    /**
     * Инвалидация токена
     */
    public void invalidateToken(String tokenId) {
        if (StringUtils.hasText(tokenId)) {
            redisTemplate.delete("token:" + tokenId);
        }
    }

    /**
     * Генерация одноразового кода подтверждения (TAN)
     */
    public String generateTan(String userId, String operationId) {
        String tan = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "tan:" + tan,
                userId + ":" + operationId,
                5, TimeUnit.MINUTES // TAN действителен 5 минут
        );
        return tan;
    }

    /**
     * Проверка TAN кода
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

        // Удаляем использованный TAN
        redisTemplate.delete(key);

        String[] parts = value.split(":");
        if (parts.length != 2) {
            return Optional.empty();
        }

        return Optional.of(Pair.of(parts[0], parts[1]));
    }

    public static class TokenGenerationException extends RuntimeException {
        public TokenGenerationException(String message) {
            super(message);
        }
    }
}