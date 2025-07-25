package com.substring.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

public class AuthChannelInterceptorAdapter implements ChannelInterceptor {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Получаем токен из заголовков
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            // Проверяем заголовок Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid authorization header: Authorization header must start with 'Bearer '");
            }

            String token = authHeader.replace("Bearer ", "");

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token.trim());
                ResponseEntity<Map> response = restTemplate.exchange(
                        authServiceUrl + "/api/v1/auth/verify",
                        HttpMethod.POST,
                        new HttpEntity<>(headers),
                        Map.class
                );

                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
                }

                Map<String, Object> body = response.getBody();
                if (body == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Empty auth response");
                }

                String username = (String) body.get("username");
                String userId = (String) body.get("userId");

                if (username == null || userId == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "User credentials not found in token");
                }

                // Устанавливаем аутентификационные данные
                accessor.setUser(() -> username);
                accessor.getSessionAttributes().put("userId", userId);
                accessor.getSessionAttributes().put("username", username);
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Authentication error: " + e.getMessage()
                );
            }
        }

        return message;
    }
}