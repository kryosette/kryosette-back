package com.substring.chat.controllers;

import com.substring.chat.entities.*;
import com.substring.chat.repositories.PrivateRoomRepository;
import com.substring.chat.repositories.RoomRepository;
import com.substring.chat.services.TypingService;
import com.substring.chat.services.TypingStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomRepository roomRepository;
    private final RestTemplate restTemplate;
    private final TypingService typingService;
    private final PrivateRoomRepository privateRoomRepository;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Room createRoom(
            @Valid @RequestBody Room room,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            Map<String, String> authData = verifyToken(token);

            if (authData.get("userId") == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "User credentials not found in token");
            }

            room.setCreatedAt(Instant.now());
            room.setUserId(authData.get("userId"));
            return roomRepository.save(room);

        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при создании комнаты: " + e.getMessage()
            );
        }
    }

    @PostMapping("/private")
    @ResponseStatus(HttpStatus.CREATED)
    public PrivateRoomDTO createPrivateRoom(
            @Valid @RequestBody CreatePrivateRoomRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        Map<String, String> authData = verifyToken(token);
        String currentUserId = authData.get("userId");
        String email = authData.get("email");

        if (!currentUserId.equals(request.getParticipant1Id()) &&
                !currentUserId.equals(request.getParticipant2Id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only create rooms where you are a participant");
        }

        Optional<PrivateRoom> existingRoom = privateRoomRepository.findPrivateRoomBetweenUsers(
                request.getParticipant1Id(),
                request.getParticipant2Id());

        if (existingRoom.isPresent()) {
            return convertToDto(existingRoom.get());
        }

        PrivateRoom room = new PrivateRoom();
        room.setType(RoomType.PRIVATE);
        room.setCreatedAt(Instant.now());
        room.setCreatedBy(currentUserId);

        room.addParticipant(request.getParticipant1Id());
        room.addParticipant(request.getParticipant2Id());

        PrivateRoom savedRoom = privateRoomRepository.save(room);
        return convertToDto(savedRoom);
    }

    private PrivateRoomDTO convertToDto(PrivateRoom room) {
        PrivateRoomDTO dto = new PrivateRoomDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setDescription(room.getDescription());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setCreatedBy(room.getCreatedBy());
        dto.setType(room.getType());

        List<String> participantIds = room.getParticipants().stream()
                .map(participant -> participant.getId().getUserId())
                .collect(Collectors.toList());
        dto.setParticipantIds(participantIds);

        return dto;
    }

    @GetMapping("/private")
    public List<PrivateRoomDTO> getUserRooms( @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");
        Map<String, String> authData = verifyToken(token);
        String currentUserId = authData.get("userId");
        return privateRoomRepository.findRoomsByUserId(currentUserId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/private")
    public PrivateRoomDTO getRoomPrivateById(@PathVariable Long id) {
        return privateRoomRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow();
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
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

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{roomId}/typing")
    public ResponseEntity<Void> setTypingStatus(
            @PathVariable Long roomId,
            @RequestBody TypingStatusDto typingStatusDto,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        typingService.setTypingStatus(roomId, typingStatusDto, token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/typing")
    public ResponseEntity<List<TypingStatusDto>> getTypingStatuses(
            @PathVariable Long roomId,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        List<TypingStatusDto> statuses = typingService.getTypingStatuses(roomId, token);
        return ResponseEntity.ok(statuses);
    }
}
