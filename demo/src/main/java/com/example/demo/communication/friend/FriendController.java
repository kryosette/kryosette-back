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
    private final Logger log = LoggerFactory.getLogger(FriendController.class);

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<FriendRequestDto> sendFriendRequest(
            @AuthenticationPrincipal User userDetails,
            @PathVariable String receiverId,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String senderId = userDetails.getEmail();
        String tokenId = authHeader.substring(7);
        try {
            FriendRequestDto request = friendService.sendFriendRequest(senderId, String.valueOf(receiverId), tokenId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            log.error("Ошибка при отправке запроса на дружбу: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
            log.error("Ошибка при принятии запроса на дружбу: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
            log.error("Ошибка при отклонении запроса на дружбу: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
            log.error("Ошибка при получении запросов на дружбу: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FriendDto>> getFriends(
            @AuthenticationPrincipal User userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            List<FriendDto> friends = friendService.getFriends(userDetails.getId());
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/remove/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal User userDetails,
            @PathVariable String friendId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            friendService.removeFriend(userDetails.getId(), friendId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
