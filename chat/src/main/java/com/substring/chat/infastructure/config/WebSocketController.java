package com.substring.chat.infastructure.config;

import com.substring.chat.applications.dtos.messages.MessageDto;
import com.substring.chat.repositories.room.RoomRepository;
import com.substring.chat.services.message.MessageService;
import com.substring.chat.applications.dtos.messages.typing.TypingStatusDto;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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