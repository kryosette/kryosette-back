package com.substring.chat.controllers;

import com.substring.chat.config.RateLimited;
import com.substring.chat.entities.*;
import com.substring.chat.services.PrivateMessageService;
import com.substring.chat.services.PrivateRoomService;
import com.substring.chat.services.TypingService;
import com.substring.chat.services.TypingStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private-rooms/{privateRoomId}/messages")
@RequiredArgsConstructor
public class PrivateMessageController {
    private final PrivateMessageService privateMessageService;
    private final TypingService typingService;
    private final PrivateRoomService privateRoomService;

    @PostMapping
    @RateLimited(5)
    public ResponseEntity<PrivateMessageDto> createPrivateMessage(
            @PathVariable Long privateRoomId,
            @RequestBody PrivateMessageDto messageDto,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");
        messageDto.setPrivateRoomId(privateRoomId);

        PrivateMessageDto createdMessage = privateMessageService.createPrivateMessage(messageDto, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    @GetMapping
    public ResponseEntity<List<PrivateMessageDto>> getPrivateMessages(
            @PathVariable Long privateRoomId,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        List<PrivateMessageDto> messages = privateMessageService.getMessagesByPrivateRoom(privateRoomId, token);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/typing")
    public ResponseEntity<Void> setTypingStatus(
            @PathVariable Long privateRoomId,
            @RequestBody TypingStatusDto typingStatusDto,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        typingService.setTypingStatus(privateRoomId, typingStatusDto, token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/typing")
    public ResponseEntity<List<TypingStatusDto>> getTypingStatuses(
            @PathVariable Long privateRoomId,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        List<TypingStatusDto> statuses = typingService.getTypingStatuses(privateRoomId, token);
        return ResponseEntity.ok(statuses);
    }


    @PostMapping("/{messageId}/reactions")
    @CacheEvict(value = "messageReactions", key = "#messageId")
    public ResponseEntity<String> addReaction(
            @PathVariable Long privateRoomId,
            @PathVariable Long messageId,
            @RequestBody String reaction,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");

        privateMessageService.addReaction(privateRoomId, messageId, reaction.trim(), token);
        return ResponseEntity.ok(reaction);
    }

    @DeleteMapping("/{messageId}/reactions/{reaction}")
    public ResponseEntity<Void> removeReaction(
            @PathVariable Long privateRoomId,
            @PathVariable Long messageId,
            @PathVariable String reaction) {

        privateMessageService.removeReaction(privateRoomId, messageId, reaction);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{messageId}/reactions")
    @Cacheable(value = "messageReactions", key = "#messageId")
    public ResponseEntity<Map<String, Long>> getReactions(
            @PathVariable Long privateRoomId,
            @PathVariable Long messageId) {

        Map<String, Long> reactions = privateMessageService.getReactions(messageId);
        return ResponseEntity.ok(reactions != null ? reactions : new HashMap<>());
    }


    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long privateRoomId,
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        privateMessageService.deleteMessage(privateRoomId, messageId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pin")
    public ResponseEntity<Void> pinMessage(
            @PathVariable Long privateRoomId,
            @RequestBody PinMessageRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        privateRoomService.pinMessage(privateRoomId, request, token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/block/{userId}")
    public ResponseEntity<Void> blockUser(
            @PathVariable Long privateRoomId,
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        privateRoomService.blockUser(privateRoomId, userId, token);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/block/{userId}")
    public ResponseEntity<Void> unblockUser(
            @PathVariable Long privateRoomId,
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        privateRoomService.unblockUser(privateRoomId, userId, token);
        return ResponseEntity.noContent().build();
    }
}