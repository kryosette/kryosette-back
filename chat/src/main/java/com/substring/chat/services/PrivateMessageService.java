package com.substring.chat.services;

import com.substring.chat.controllers.MessageDto;
import com.substring.chat.entities.*;
import com.substring.chat.repositories.MessageRepository;
import com.substring.chat.repositories.PrivateMessageRepository;
import com.substring.chat.repositories.PrivateRoomRepository;
import com.substring.chat.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivateMessageService {
    private final PrivateMessageRepository privateMessageRepository;
    private final PrivateRoomRepository privateRoomRepository;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Transactional
    public PrivateMessageDto createPrivateMessage(PrivateMessageDto messageDto, String token) {
        // Verify token and get user info
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());
        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SecurityException("Authentication failed: " + response.getBody());
        }

        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");
        String username = (String) response.getBody().get("username");

        if (userId == null) {
            throw new RuntimeException("User ID not found in token");
        }

        // Verify user is participant of the private room
        PrivateRoom room = privateRoomRepository.findById(messageDto.getPrivateRoomId())
                .orElseThrow(() -> new RuntimeException("Private room not found"));

        boolean isParticipant = room.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isParticipant) {
            throw new SecurityException("User is not a participant of this private room");
        }

        // Create and save message
        PrivateMessage message = new PrivateMessage();
        message.setContent(messageDto.getContent());
        message.setPrivateRoom(room);
        message.setUserId(userId);
        message.setSender(username);
        message.setTimestamp(Instant.now());

        PrivateMessage savedMessage = privateMessageRepository.save(message);
        return convertToDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<PrivateMessageDto> getMessagesByPrivateRoom(Long privateRoomId, String token) {
        // Verify token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());
        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SecurityException("Authentication failed");
        }

        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");

        // Verify user is participant
        PrivateRoom room = privateRoomRepository.findById(privateRoomId)
                .orElseThrow(() -> new RuntimeException("Private room not found"));

        boolean isParticipant = room.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isParticipant) {
            throw new SecurityException("User is not a participant of this private room");
        }

        // Get messages
        List<PrivateMessage> messages = privateMessageRepository.findByPrivateRoomIdOrderByTimestampAsc(privateRoomId);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PrivateMessageDto convertToDto(PrivateMessage message) {
        PrivateMessageDto dto = new PrivateMessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSender(message.getSender());
        dto.setPrivateRoomId(message.getPrivateRoom().getId());
        dto.setUserId(message.getUserId());
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }
}