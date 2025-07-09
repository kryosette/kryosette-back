package com.example.demo.user.notifications;

import com.example.demo.security.opaque_tokens.TokenService;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userServiceClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final TokenService tokenService;

    @Transactional
    public Notification createNotification(String recipientEmail, String senderEmail,
                                           NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setRecipientEmail(recipientEmail);
        notification.setSenderEmail(senderEmail);
        notification.setType(type);
        notification.setMessage(message);

        Notification saved = notificationRepository.save(notification);
        sendRealTimeNotification(saved);
        return saved;
    }

    private void sendRealTimeNotification(Notification notification) {
        messagingTemplate.convertAndSendToUser(
                notification.getRecipientEmail(),
                "/queue/notifications",
                convertToDto(notification)
        );
    }

//    @Transactional(readOnly = true)
//    public Page<NotificationDto> getUserNotifications(String email, Pageable pageable) {
//        return notificationRepository.findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(email, pageable)
//                .map(this::convertToDto);
//    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(String token, Pageable pageable) {
        String userEmail = String.valueOf(tokenService.getTokenJsonData(token));

        return notificationRepository.findByRecipientEmail(userEmail, pageable)
                .map(this::convertToDto);
    }

    @Transactional
    public void markNotificationsAsRead(List<Long> ids, String email) {
        notificationRepository.markAsRead(ids, email);
    }

    public long getUnreadCount(String email) {
        return notificationRepository.countUnreadNotifications(email);
    }

    @Scheduled(cron = "0 0 3 * * ?") // Каждый день в 3:00
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldNotifications(cutoff);
    }

    private NotificationDto convertToDto(Notification notification) {
        UserDto senderDto = userServiceClient.findByEmail(notification.getSenderEmail())
                .map(user -> new UserDto(
                        user.getUsername(),
                        user.getEmail()
                ))
                .orElse(null);

        return NotificationDto.builder()
                .sender(senderDto)
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }


}