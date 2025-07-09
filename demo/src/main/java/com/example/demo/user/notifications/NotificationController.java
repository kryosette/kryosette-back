package com.example.demo.user.notifications;

import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @RequestHeader("Authorization") String authHeader,
            Pageable pageable) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header");
        }

        String token = authHeader.substring(7);
        return ResponseEntity.ok(notificationService.getUserNotifications(token, pageable));
    }

//    @GetMapping("/unread-count")
//    public ResponseEntity<Long> getUnreadCount(
//            @RequestHeader("Authorization") String authHeader) {
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
//        }
//        String token = authHeader.substring(7);
//        Optional<User> userOptional = userRepository.findByEmail(emailId);
//
//        if (userOptional.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        User user = userOptional.get();
//        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
//    }

//    @PostMapping("/{id}/read")
//    public ResponseEntity<Void> markAsRead(
//            @RequestHeader("Authorization") String token,
//            @PathVariable String id) {
//
//        String userId = extractUserIdFromToken(token);
//        notificationService.markAsRead(id, userId);
//        return ResponseEntity.noContent().build();
//    }

//    @PostMapping("/mark-all-read")
//    public ResponseEntity<Void> markAllAsRead(
//            @RequestHeader("Authorization") String token) {
//
//        String userId = extractUserIdFromToken(token);
//        notificationService.markAllAsRead(userId);
//        return ResponseEntity.noContent().build();
//    }

//    // WebSocket endpoint для тестирования (в реальном приложении вызывается из других сервисов)
//    @PostMapping("/test-notification")
//    public ResponseEntity<Void> sendTestNotification(
//            @RequestHeader("Authorization") String token) {
//
//        String userId = extractUserIdFromToken(token);
//        NotificationDto notification = notificationService.createTestNotification(userId);
//
//        // Отправка через WebSocket
//        messagingTemplate.convertAndSendToUser(
//                userId,
//                "/queue/notifications",
//                notification
//        );
//
//        return ResponseEntity.ok().build();
//    }
//
//    private String extractUserIdFromToken(String token) {
//        // Реализуйте извлечение userId из JWT токена
//        return jwtTokenProvider.getUserIdFromToken(token.replace("Bearer ", ""));
//    }
}