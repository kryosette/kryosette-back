package com.example.demo.communication.friend;

import com.example.demo.user.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;
    private final Logger log = LoggerFactory.getLogger(FriendController.class); // Добавили Logger

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<CompletableFuture<FriendRequestDto>> sendFriendRequest(
            @AuthenticationPrincipal User userDetails,
            @PathVariable String receiverId,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String senderId = userDetails.getId();
        String tokenId = authHeader.substring(7);
        try {
            CompletableFuture<FriendRequestDto> request = friendService.sendFriendRequest(senderId, String.valueOf(receiverId));
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            if (e instanceof org.apache.catalina.connector.ClientAbortException ||
                    e instanceof org.springframework.web.util.NestedServletException &&
                            e.getCause() instanceof org.apache.catalina.connector.ClientAbortException) {
                // Клиент отключился, игнорируем исключение
                log.warn("Клиент отключился во время отправки запроса на дружбу: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build(); // Или другой подходящий статус
            } else {
                // Другая ошибка, логируем и возвращаем ошибку клиенту
                log.error("Ошибка при отправке запроса на дружбу: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<FriendRequestDto> acceptFriendRequest(
            @AuthenticationPrincipal User userDetails,
            @PathVariable String requestId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String currentUserId = userDetails.getId();
        try {
            FriendRequestDto request = friendService.acceptFriendRequest(requestId, currentUserId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            if (e instanceof org.apache.catalina.connector.ClientAbortException ||
                    e instanceof org.springframework.web.util.NestedServletException &&
                            e.getCause() instanceof org.apache.catalina.connector.ClientAbortException) {
                // Клиент отключился, игнорируем исключение
                log.warn("Клиент отключился во время принятия запроса на дружбу: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            } else {
                // Другая ошибка, логируем и возвращаем ошибку клиенту
                log.error("Ошибка при принятии запроса на дружбу: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Void> rejectFriendRequest(
            @AuthenticationPrincipal User userDetails,
            @PathVariable String requestId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String receiverId = userDetails.getId();
        try {
            friendService.rejectFriendRequest(requestId, receiverId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e instanceof org.apache.catalina.connector.ClientAbortException ||
                    e instanceof org.springframework.web.util.NestedServletException &&
                            e.getCause() instanceof org.apache.catalina.connector.ClientAbortException) {
                // Клиент отключился, игнорируем исключение
                log.warn("Клиент отключился во время отклонения запроса на дружбу: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            } else {
                // Другая ошибка, логируем и возвращаем ошибку клиенту
                log.error("Ошибка при отклонении запроса на дружбу: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendRequestDto>> getPendingRequests(
            @AuthenticationPrincipal User userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userId = userDetails.getId();
        try {
            List<FriendRequestDto> requests = friendService.getPendingRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            if (e instanceof org.apache.catalina.connector.ClientAbortException ||
                    e instanceof org.springframework.web.util.NestedServletException &&
                            e.getCause() instanceof org.apache.catalina.connector.ClientAbortException) {
                // Клиент отключился, игнорируем исключение
                log.warn("Клиент отключился во время получения запросов на дружбу: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            } else {
                // Другая ошибка, логируем и возвращаем ошибку клиенту
                log.error("Ошибка при получении запросов на дружбу: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
}
