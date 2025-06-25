package com.example.demo.communication.chat;

import com.example.demo.communication.chat.message.Message;
import com.example.demo.communication.chat.message.MessageDto;
import com.example.demo.communication.chat.room.Room;
import com.example.demo.communication.chat.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("chats")
@RequiredArgsConstructor
@Tag(name = "Chats")
@CrossOrigin("http://localhost:3000")
public class ChatController {
//    private final ChatService chatService;
    private final RoomRepository roomRepository;

//    @PostMapping
//    public ResponseEntity<ChatDto> createChat(
//            @RequestBody CreateChatDto chatDto,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        ChatDto createdChat = chatService.createChat(chatDto, userDetails.getUsername());
//        return ResponseEntity.status(HttpStatus.CREATED).body(createdChat);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<ChatDto>> getAllChats() {
//        return ResponseEntity.ok(chatService.getAllChats());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ChatDto> getChatById(
//            @PathVariable Long id
//    ) {
//        return ResponseEntity.ok(chatService.getChatById(id));
//    }

    @MessageMapping("/sendMessage/{roomId}")
    @SendTo("topic/room/{roomId}")
    public Message sendMessage(
        @DestinationVariable String roomId,
        @RequestBody MessageDto dto
    ) {
        Room room = roomRepository.findByRoomId(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("room not found !!"));

        Message message = new Message();
        message.setContent(dto.getContent());
        message.setSender(dto.getSender());
        message.setTimeStamp(LocalDateTime.now());

        room.getMessages().add(message);
        roomRepository.save(room);

        return message;
    }
}