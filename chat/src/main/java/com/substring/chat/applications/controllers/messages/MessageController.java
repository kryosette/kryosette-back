package com.substring.chat.applications.controllers.messages;

import com.substring.chat.infastructure.config.RateLimited;
import com.substring.chat.applications.dtos.messages.MessageDto;
import com.substring.chat.services.message.typing.TypingService;
import com.substring.chat.services.message.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/rooms/{roomId}/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final TypingService typingService;

    @PostMapping
    @RateLimited(5)
    public ResponseEntity<MessageDto> createMessage(
            @PathVariable Long roomId,
            @RequestBody MessageDto messageDto) {
        messageDto.setRoomId(roomId);

        MessageDto createdMessage = messageService.createMessage(messageDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    @GetMapping
    public ResponseEntity<List<MessageDto>> getMessagesByRoom(
            @PathVariable Long roomId,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        List<MessageDto> messages = messageService.getMessagesByRoom(roomId, token);
        return ResponseEntity.ok(messages);
    }

}