package com.example.demo.user.profile.status;

import com.example.demo.security.opaque_tokens.TokenService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.Header;

@Controller
@RequiredArgsConstructor
public class StatusController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @MessageMapping("/status.update")
    public void updateStatus(@Header("Authorization") String authHeader, Principal principal, StatusMessage message) {

        // 1. Проверка заголовка Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header");
        }

        // 2. Извлечение токена из заголовка
        String token = authHeader.substring(7);

        // 3. Проверка токена с использованием TokenService
        try {
            String userEmail = String.valueOf(tokenService.getTokenJsonData(token));

            // 4. Получение пользователя из базы данных
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

            // 5. Обновление статуса пользователя
            user.setOnline(message.isOnline());
            user.setLastSeenAt(LocalDateTime.now());
            userRepository.save(user);

            // 6. Отправка обновления статуса
            messagingTemplate.convertAndSend(
                    "/topic/status/" + user.getId(),
                    message
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }
}