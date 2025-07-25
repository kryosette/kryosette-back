package com.substring.chat.config;

import com.substring.chat.controllers.MessageDto;
import com.substring.chat.repositories.RoomRepository;
import com.substring.chat.services.MessageService;
import com.substring.chat.services.TypingStatusDto;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final RoomRepository roomRepository;

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                               MessageService messageService,
                               RoomRepository roomRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.roomRepository = roomRepository;
    }

    @MessageMapping("/chat/{roomId}/send")
    @SendTo("/topic/room/{roomId}")
    public MessageDto sendMessage(
            @DestinationVariable Long roomId,
            @Payload MessageDto messageDto) {

        messageDto.setRoomId(roomId);
        return messageService.createMessage(messageDto);
    }

    @MessageMapping("/chat/{roomId}/typing")
    @SendTo("/topic/room/{roomId}/typing")
    public TypingStatusDto handleTyping(
            @DestinationVariable Long roomId,
            TypingStatusDto typingStatus) {

        return typingStatus;
    }
}