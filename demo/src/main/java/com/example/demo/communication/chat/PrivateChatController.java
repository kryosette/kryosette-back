package com.example.demo.communication.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

//@Controller
//@RequiredArgsConstructor
//public class PrivateChatController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final ChatService chatService;
//
//    @MessageMapping("/private-chat/{chatId}")
//    public void processPrivateMessage(
//            @Payload MessageDto messageDto,
//            @DestinationVariable Long chatId,
//            Principal principal) {
//
//        Message message = chatService.savePrivateMessage(
//                chatId,
//                principal.getName(),
//                messageDto.getContent()
//        );
//
//        messagingTemplate.convertAndSendToUser(
//                message.getRecipient().getUsername(),
//                "/queue/private",
//                message
//        );
//    }
//}
