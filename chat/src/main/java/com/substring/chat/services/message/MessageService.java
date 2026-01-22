package com.substring.chat.services.message;

import com.substring.chat.applications.dtos.messages.MessageDto;
import com.substring.chat.domain.model.messages.Message;
import com.substring.chat.domain.model.room.Room;
import com.substring.chat.repositories.message.MessageRepository;
import com.substring.chat.repositories.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Transactional
    public MessageDto createMessage(MessageDto messageDto) {
        Room room = roomRepository.findById(messageDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Message message = new Message();
        message.setContent(messageDto.getContent());
        message.setRoom(room);
        message.setSender(messageDto.getSender() != null ? messageDto.getSender() : "Anonymous");
        message.setTimestamp(Instant.now());

        Message savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }

//    @Transactional
//    public MessageDto createMessage(MessageDto messageDto, String token) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token.trim());
//        ResponseEntity<Map> response = restTemplate.exchange(
//                authServiceUrl + "/api/v1/auth/verify",
//                HttpMethod.POST,
//                new HttpEntity<>(headers),
//                Map.class
//        );
//
//        if (!response.getStatusCode().is2xxSuccessful()) {
//            throw new SecurityException("Ошибка авторизации: " + response.getBody());
//        }
//
//        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");
//        String username = (String) response.getBody().get("username");
//        if (userId == null) {
//            throw new RuntimeException("User ID не найден в токене");
//        }
//
//        Room room = roomRepository.findById(messageDto.getRoomId())
//                .orElseThrow(() -> new RuntimeException("Room not found"));
//
//        Message message = new Message();
//        message.setContent(messageDto.getContent());
//        message.setRoom(room);
//        message.setUserId(userId);
//        message.setSender(username);
//        message.setTimestamp(Instant.now());
//
//        Message savedMessage = messageRepository.save(message);
//        return convertToDto(savedMessage);
//    }

    private MessageDto convertToDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setContent(message.getContent());
        dto.setSender(message.getSender());
        dto.setRoomId(message.getRoom().getId());
        dto.setUserId(message.getUserId());
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByRoom(Long roomId, String token) {


        List<Message> messages = messageRepository.findByRoomIdOrderByTimestampAsc(roomId);

        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}