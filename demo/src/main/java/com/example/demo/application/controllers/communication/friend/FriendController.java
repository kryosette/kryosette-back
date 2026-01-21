package com.example.demo.application.controllers.communication.friend;

import com.example.demo.application.dtos.communication.friend.FriendDto;
import com.example.demo.domain.requests.communication.friend.FriendRequestDto;
import com.example.demo.domain.services.communication.friend.FriendService;
import com.example.demo.domain.model.user.subscription.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing friend relationships including:
 * - Sending/accepting/rejecting friend requests
 * - Listing friends and pending requests
 * - Removing friends
 *
 * <p>All endpoints require authentication via Spring Security.
 */
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    /**
     * Sends a friend request from authenticated user to specified receiver
     * @param userDetails Authenticated user from security context
     * @param receiverId Target user ID for friend request
     * @param authHeader Bearer token for request validation
     * @return Created friend request or error status
     */
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<FriendRequestDto> sendFriendRequest(
            @AuthenticationPrincipal User userDetails,
            @PathVariable String receiverId,
            @RequestHeader("Authorization") String authHeader) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String senderId = userDetails.getEmail();
        String tokenId = authHeader.substring(7);
        try {
            FriendRequestDto request = friendService.sendFriendRequest(senderId, String.valueOf(receiverId), tokenId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Accepts pending friend request
     * @param userDetails Authenticated user from security context
     * @param requestId ID of friend request to accept
     * @return Accepted friend request or error status
     */
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Rejects pending friend request
     * @param userDetails Authenticated user from security context
     * @param requestId ID of friend request to reject
     * @return Empty response or error status
     */
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all pending friend requests for authenticated user
     * @param userDetails Authenticated user from security context
     * @return List of pending requests or error status
     */
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves current friends list for authenticated user
     * @param userDetails Authenticated user from security context
     * @return List of friends or error status
     */
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

    /**
     * Removes friend relationship between authenticated user and specified friend
     * @param userDetails Authenticated user from security context
     * @param friendId ID of friend to remove
     * @return Empty response or error status
     */
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
