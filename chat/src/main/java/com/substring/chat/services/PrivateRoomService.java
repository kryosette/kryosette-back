package com.substring.chat.services;

import com.substring.chat.entities.PinMessageRequest;
import com.substring.chat.entities.PrivateMessage;
import com.substring.chat.entities.PrivateRoom;
import com.substring.chat.repositories.PrivateMessageRepository;
import com.substring.chat.repositories.PrivateRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrivateRoomService {
    private final PrivateRoomRepository privateRoomRepository;
    private final PrivateMessageRepository privateMessageRepository;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Transactional
    public void pinMessage(Long roomId, PinMessageRequest request, String token) {
        Map<String, String> authData = verifyToken(token);
        String userId = authData.get("userId");

        PrivateRoom room = privateRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));


        if (!room.isParticipant(userId)) {
            throw new SecurityException("You are not a participant of this room");
        }

        if (request.isPin()) {
            PrivateMessage message = privateMessageRepository.findById(request.getMessageId())
                    .orElseThrow(() -> new RuntimeException("Message not found"));
            room.setPinnedMessage(message);
        } else {
            room.setPinnedMessage(null);
        }

        privateRoomRepository.save(room);
    }

    @Transactional
    public void blockUser(Long roomId, String userIdToBlock, String token) {
        Map<String, String> authData = verifyToken(token);
        String currentUserId = authData.get("userId");

        PrivateRoom room = privateRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isParticipant(currentUserId)) {
            throw new SecurityException("You are not a participant of this room");
        }

        room.blockUser(userIdToBlock);
        privateRoomRepository.save(room);
    }

    @Transactional
    public void unblockUser(Long roomId, String userIdToUnblock, String token) {
        Map<String, String> authData = verifyToken(token);
        String currentUserId = authData.get("userId");

        PrivateRoom room = privateRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isParticipant(currentUserId)) {
            throw new SecurityException("You are not a participant of this room");
        }

        room.unblockUser(userIdToUnblock);
        privateRoomRepository.save(room);
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
}
