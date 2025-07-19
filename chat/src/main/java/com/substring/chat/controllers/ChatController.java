//package com.substring.chat.controllers;
//
//import com.substring.chat.entities.ChatMessage;
//import com.substring.chat.entities.Message;
//import com.substring.chat.entities.Room;
//import com.substring.chat.playload.MessageRequest;
//import com.substring.chat.repositories.MessageRepository;
//import com.substring.chat.repositories.RoomRepository;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.MessageDeliveryException;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.springframework.messaging.handler.annotation.*;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.server.ResponseStatusException;
//
//@RestController
//@RequestMapping("/api/rooms/{roomId}/messages")
//@RequiredArgsConstructor
//public class ChatController {
//    private final MessageRepository messageRepository;
//    private final RoomRepository roomRepository;
//
//    @PostMapping
//    public ResponseEntity<Message> createMessage(
//            @PathVariable Long roomId,
//            @RequestBody MessageDto messageDto,
//            @RequestHeader("Authorization") String authHeader) {
//
//        // Проверка авторизации
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
//        }
//
//        // Находим комнату
//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
//
//        // Создаем сообщение
//        Message message = new Message();
//        message.setContent(messageDto.getContent());
//        message.setSender(messageDto.getSender());
//        message.setRoom(room); // Важно установить комнату!
//        message.setUserId(messageDto.getUserId());
//        message.setTimestamp(Instant.now());
//
//        // Сохраняем
//        Message savedMessage = messageRepository.save(message);
//        return ResponseEntity.ok(savedMessage);
//    }
//}
