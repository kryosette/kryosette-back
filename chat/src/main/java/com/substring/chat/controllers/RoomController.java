package com.substring.chat.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.substring.chat.config.ClearCache;
import com.substring.chat.entities.*;
import com.substring.chat.repositories.PrivateRoomRepository;
import com.substring.chat.repositories.RoomRepository;
import com.substring.chat.services.PrivateMessageService;
import com.substring.chat.services.PrivateRoomService;
import com.substring.chat.services.TypingService;
import com.substring.chat.services.TypingStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CacheConfig(cacheNames = "rooms")
@Slf4j
public class RoomController {
    private final RoomRepository roomRepository;
    private final RestTemplate restTemplate;
    private final TypingService typingService;
    private final PrivateRoomRepository privateRoomRepository;
    private final UserBlockRepository userBlockRepository;
    private final PrivateMessageService privateMessageService;
    private final PrivateRoomService privateRoomService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper; // for serialization JSON

    // keys for redis
    private static final String USER_ROOMS_KEY_PREFIX = "user_rooms:";
    private static final String PRIVATE_ROOM_KEY_PREFIX = "private_room:";

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${chat.cache.expiration:3600000}")
    private long chatCacheExpiration;


    private void clearUserRoomsCache(String userId) {
        try {
            redisTemplate.delete(USER_ROOMS_KEY_PREFIX + userId);
            log.debug("Cleared rooms cache for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to clear cache for user {}", userId, e);
        }
    }

    private void clearPrivateRoomCache(Long roomId) {
        try {
            redisTemplate.delete(PRIVATE_ROOM_KEY_PREFIX + roomId);
            log.debug("Cleared cache for room: {}", roomId);
        } catch (Exception e) {
            log.error("Failed to clear cache for room {}", roomId, e);
        }
    }

    private void cacheRoom(Room room) {
        try {
            String cacheKey = "room:" + room.getId();
            redisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(room),
                    chatCacheExpiration,
                    TimeUnit.MILLISECONDS
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to cache room {}", room.getId(), e);
        }
    }


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
            Room savedRoom = roomRepository.save(room);

            cacheRoom(savedRoom);

            redisTemplate.delete("all_rooms");

            return savedRoom;

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

        if (!currentUserId.equals(request.getParticipant1Id()) &&
                !currentUserId.equals(request.getParticipant2Id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only create rooms where you are a participant");
        }

        Optional<PrivateRoom> existingRoom = privateRoomRepository.findPrivateRoomBetweenUsers(
                request.getParticipant1Id(),
                request.getParticipant2Id());

        clearUserRoomsCache(request.getParticipant1Id());
        clearUserRoomsCache(request.getParticipant2Id());

        if (existingRoom.isPresent()) {
            clearPrivateRoomCache(existingRoom.get().getId());
            return convertToDto(existingRoom.get());
        }

        PrivateRoom room = new PrivateRoom();
        room.setType(RoomType.PRIVATE);
        room.setCreatedAt(Instant.now());
        room.setCreatedBy(currentUserId);

        room.addParticipant(request.getParticipant1Id());
        room.addParticipant(request.getParticipant2Id());

        redisTemplate.delete("user_rooms:" + currentUserId);

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
    public List<PrivateRoomDTO> getUserRooms(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Map<String, String> authData = verifyToken(token);
        String userId = authData.get("userId");

        String cacheKey = USER_ROOMS_KEY_PREFIX + userId;
        String cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            try {
                return objectMapper.readValue(cachedData, new TypeReference<List<PrivateRoomDTO>>() {});
            } catch (JsonProcessingException e) {
                log.error("Failed to parse cached rooms", e);
            }
        }

        List<PrivateRoomDTO> rooms = privateRoomRepository.findRoomsByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(rooms),
                    chatCacheExpiration,
                    TimeUnit.MILLISECONDS
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to cache rooms", e);
        }

        return rooms;
    }

    @GetMapping("/{id}/private")
    public PrivateRoomDTO getRoomPrivateById(@PathVariable Long id) {
        String cacheKey = PRIVATE_ROOM_KEY_PREFIX + id;
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData, PrivateRoomDTO.class);
            }
        } catch (Exception e) {
            log.error("Failed to read cached room {}", id, e);
        }

        PrivateRoomDTO room = privateRoomRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow();

        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(room),
                    chatCacheExpiration,
                    TimeUnit.MILLISECONDS
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to cache room", e);
        }

        return room;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        final String CACHE_KEY = "all_rooms";

        try {
            String cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cachedData != null) {
                log.info("✅ Данные получены из кеша Redis");
                return objectMapper.readValue(cachedData, new TypeReference<List<Room>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to parse cached rooms", e);
        }

        log.info("⏳ Данные загружаются из БД");
        List<Room> rooms = roomRepository.findAll();

        try {
            redisTemplate.opsForValue().set(
                    CACHE_KEY,
                    objectMapper.writeValueAsString(rooms),
                    chatCacheExpiration,
                    TimeUnit.MILLISECONDS
            );
            log.debug("Cached all rooms");
        } catch (JsonProcessingException e) {
            log.error("Failed to cache rooms", e);
        }

        return rooms;
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
        final String CACHE_KEY = "room:" + id;

        // Пытаемся получить из кеша
        try {
            String cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cachedData != null) {
                log.debug("✅ Room {} from cache", id);
                return ResponseEntity.ok(objectMapper.readValue(cachedData, Room.class));
            }
        } catch (Exception e) {
            log.error("Cache read error for room {}", id, e);
        }

        // Загрузка из БД
        Optional<Room> room = roomRepository.findById(id);

        if (room.isPresent()) {
            // Сохраняем в кеш
            try {
                redisTemplate.opsForValue().set(
                        CACHE_KEY,
                        objectMapper.writeValueAsString(room.get()),
                        chatCacheExpiration,
                        TimeUnit.MILLISECONDS
                );
            } catch (Exception e) {
                log.error("Failed to cache room {}", id, e);
            }
            return ResponseEntity.ok(room.get());
        }

        return ResponseEntity.notFound().build();
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
