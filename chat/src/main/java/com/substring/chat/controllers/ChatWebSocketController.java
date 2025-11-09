//package com.substring.chat.controllers;
//
//import com.substring.chat.entities.Message;
//import com.substring.chat.services.MessageService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//@Controller
//@RequiredArgsConstructor
//public class ChatWebSocketController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final MessageService messageService;
//
//    @MessageMapping("/rooms/{roomId}/messages")
//    public void handleChatMessage(
//            @DestinationVariable String roomId,
//            MessageDto messageDto) {
//
//        // Сохраняем сообщение через сервис
//        MessageDto savedMessage = messageService.createMessage(messageDto, roomId);
//
//        // Отправляем сообщение всем подписчикам комнаты
//        messagingTemplate.convertAndSend("/topic/room." + roomId, savedMessage);
//    }
//}