package com.substring.chat.controllers;

import com.substring.chat.config.RateLimited;
import com.substring.chat.entities.*;
import com.substring.chat.repositories.PrivateRoomRepository;
import com.substring.chat.repositories.RoomRepository;
import com.substring.chat.services.PrivateMessageService;
import com.substring.chat.services.TypingService;
import com.substring.chat.services.TypingStatusDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/private-rooms/{privateRoomId}/messages")
@RequiredArgsConstructor
public class PrivateMessageController {
    private final PrivateMessageService privateMessageService;
    private final TypingService typingService;

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
}