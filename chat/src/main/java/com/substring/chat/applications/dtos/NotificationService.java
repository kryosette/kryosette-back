//package com.substring.chat.applications.dtos;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class NotificationService {
//    private final NotificationRepository notificationRepository;
//    private final RestTemplate restTemplate;
//
//    @Value("${auth.service.url}")
//    private String authServiceUrl;
//
//    @Transactional
//    public void createNewMessageNotification(String senderId, Long privateRoomId, Long messageId, String messagePreview, String token) {
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
//            throw new SecurityException("Authentication failed");
//        }
//
//        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");
//
//        Notification notification = Notification.builder()
//                .recipientId(recipientId)
//                .senderId(senderId)
//                .content("Новое сообщение: " + messagePreview)
//                .isRead(false)
//                .createdAt(Instant.now())
//                .type(NotificationType.NEW_PRIVATE_MESSAGE)
//                .relatedEntityId(messageId)
//                .build();
//
//        notificationRepository.save(notification);
//    }
//
//    public List<NotificationDto> getUserNotifications(String userId) {
//        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
//        return notifications.stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }
//
//    // Пометить уведомления как прочитанные
//    @Transactional
//    public void markAsRead(List<Long> notificationIds) {
//        notificationRepository.markAsRead(notificationIds);
//    }
//
//    private NotificationDto convertToDto(Notification notification) {
//
//        String senderUsername = getUsernameFromUserService(notification.getSenderId());
//
//        return NotificationDto.builder()
//                .id(notification.getId())
//                .senderUsername(senderUsername)
//                .content(notification.getContent())
//                .isRead(notification.isRead())
//                .createdAt(notification.getCreatedAt())
//                .type(notification.getType())
//                .relatedEntityId(notification.getRelatedEntityId())
//                .build();
//    }
//
//}