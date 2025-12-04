package com.example.demo.security.opaque_tokens;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceDev {

    private final KryocacheClient kryocacheClient;

    /**
     * Создание нового токена
     */
    public String createToken(String userId, String userData, int ttlSeconds) {
        String token = generateToken();

        // Формат: userId|timestamp|userData
        String data = userId + "|" + System.currentTimeMillis() + "|" + userData;

        if (kryocacheClient.setToken(token, data, ttlSeconds)) {
            log.info("Token created for user: {}, token: {}", userId, token);
            return token;
        }

        log.error("Failed to create token for user: {}", userId);
        return null;
    }

    /**
     * Валидация токена
     */
    public boolean validateToken(String token) {
        return kryocacheClient.validateToken(token);
    }

    /**
     * Получение данных по токену
     */
    public TokenDataDev getTokenData(String token) {
        String data = kryocacheClient.getToken(token);
        if (data == null) {
            return null;
        }

        String[] parts = data.split("\\|", 3);
        if (parts.length >= 3) {
            return TokenDataDev.builder()
                    .userId(parts[0])
                    .creationTime(Long.parseLong(parts[1]))
                    .userData(parts[2])
                    .build();
        }

        return null;
    }

    /**
     * Удаление токена
     */
    public boolean revokeToken(String token) {
        return kryocacheClient.delete(token);
    }

    /**
     * Обновление TTL токена
     */
    public boolean refreshToken(String token, int newTtlSeconds) {
        TokenDataDev data = getTokenData(token);
        if (data == null) {
            return false;
        }

        String newData = data.getUserId() + "|" +
                System.currentTimeMillis() + "|" +
                data.getUserData();

        return kryocacheClient.setToken(token, newData, newTtlSeconds);
    }

    /**
     * Генерация уникального токена
     */
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                System.currentTimeMillis();
    }
}
