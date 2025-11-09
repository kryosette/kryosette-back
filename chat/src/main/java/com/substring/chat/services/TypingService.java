package com.substring.chat.services;

import com.substring.chat.controllers.MessageDto;
import com.substring.chat.entities.Message;
import com.substring.chat.entities.Room;
import com.substring.chat.repositories.MessageRepository;
import com.substring.chat.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypingService {
    private final RestTemplate restTemplate;
    private final ConcurrentHashMap<Long, List<TypingStatus>> typingStatuses = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public void setTypingStatus(Long roomId, TypingStatusDto typingStatusDto, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());
        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SecurityException("Ошибка авторизации: " + response.getBody());
        }

        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");
        String username = (String) response.getBody().get("username");
        if (userId == null) {
            throw new RuntimeException("User ID не найден в токене");
        }

        TypingStatus status = new TypingStatus(
                userId,
                username,
                typingStatusDto.isTyping(),
                System.currentTimeMillis()
        );

        typingStatuses.compute(roomId, (key, statusList) -> {
            if (statusList == null) {
                statusList = new CopyOnWriteArrayList<>();
            }

            // Удаляем старый статус пользователя, если есть
            statusList.removeIf(s -> s.getUserId().equals(status.getUserId()));

            if (status.isTyping()) {
                statusList.add(status);
            }

            return statusList;
        });

        // Очищаем устаревшие статусы (если пользователь не обновлял статус более 5 секунд)
        scheduler.schedule(() -> {
            typingStatuses.computeIfPresent(roomId, (key, statusList) -> {
                long now = System.currentTimeMillis();
                statusList.removeIf(s -> now - s.getLastUpdate() > 5000);
                return statusList.isEmpty() ? null : statusList;
            });
        }, 5, TimeUnit.SECONDS);
    }

    public List<TypingStatusDto> getTypingStatuses(Long roomId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());
        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SecurityException("Ошибка авторизации: " + response.getBody());
        }

        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");
        String username = (String) response.getBody().get("username");
        if (userId == null) {
            throw new RuntimeException("User ID не найден в токене");
        }

        List<TypingStatus> statuses = typingStatuses.getOrDefault(roomId, Collections.emptyList());
        long now = System.currentTimeMillis();

        // Фильтруем только активные статусы (обновленные в последние 5 секунд)
        return statuses.stream()
                .filter(s -> now - s.getLastUpdate() <= 5000)
                .map(s -> new TypingStatusDto(s.getUserId(), s.getUsername(), s.isTyping()))
                .collect(Collectors.toList());
    }

//    private Map<String, String> verifyToken(String token) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token.trim());
//        ResponseEntity<Map> response = restTemplate.exchange(
//                authServiceUrl + "/api/v1/auth/verify",
//                HttpMethod.POST,
//                new HttpEntity<>(headers),
//                Map.class
//        );
//
//        if (!response.getStatusCode().is2xxSuccessful()) {
//            throw new SecurityException("Ошибка авторизации");
//        }
//
//        Map<String, String> result = new HashMap<>();
//        result.put("userId", (String) response.getBody().get("userId"));
//        result.put("username", (String) response.getBody().get("username"));
//        return result;
//    }
}