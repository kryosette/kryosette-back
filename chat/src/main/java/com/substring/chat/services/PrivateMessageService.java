package com.substring.chat.services;

import com.substring.chat.entities.*;
import com.substring.chat.repositories.*;
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
    private final MessageReactionRepository messageReactionRepository;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Transactional
    public PrivateMessageDto createPrivateMessage(PrivateMessageDto messageDto, String token) {
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

        PrivateRoom room = privateRoomRepository.findById(messageDto.getPrivateRoomId())
                .orElseThrow(() -> new RuntimeException("Private room not found"));

        boolean isParticipant = room.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isParticipant) {
            throw new SecurityException("User is not a participant of this private room");
        }

        PrivateMessage message = new PrivateMessage();
        message.setContent(messageDto.getContent());
        message.setPrivateRoom(room);
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
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }

    private Map<String, String> verifyToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());

        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SecurityException("Authentication failed: " + response.getStatusCode());
        }

        Map<String, String> body = response.getBody();
        if (body == null) {
            throw new SecurityException("Empty response from auth service");
        }

        return body;
    }

    public void addReaction(Long roomId, Long messageId, String reaction, String token) {
        Map<String, String> authData = verifyToken(token);

        if (!privateMessageRepository.existsByIdAndPrivateRoomId(messageId, roomId)) {
            throw new RuntimeException("Message not found");
        }
        MessageReaction mr = new MessageReaction();
        mr.setReaction(reaction);

        PrivateMessage message = privateMessageRepository.getReferenceById(messageId);
        mr.setMessage(message);

        messageReactionRepository.save(mr);
    }

    public void removeReaction(Long roomId, Long messageId, String reaction) {
        if (!privateMessageRepository.existsByIdAndPrivateRoomId(messageId, roomId)) {
            throw new RuntimeException("Message not found");
        }

        messageReactionRepository.deleteByMessageIdAndReaction(messageId, reaction);
    }

    public Map<String, Long> getReactions(Long messageId) {
        List<Object[]> results = messageReactionRepository.countReactionsByMessageId(messageId);
        return results.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    public List<MessageReaction> getMessageReactions(Long messageId) {
        return messageReactionRepository.findByMessageId(messageId);
    }

    @Transactional
    public void deleteMessage(Long roomId, Long messageId, String token) {
        Map<String, String> authData = verifyToken(token);
        String userId = authData.get("userId");

        PrivateMessage message = privateMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Проверяем, что пользователь - автор сообщения
        if (!message.getSender().equals(userId)) {
            throw new SecurityException("You can only delete your own messages");
        }

        message.setDeleted(true);
        privateMessageRepository.save(message);
    }
}