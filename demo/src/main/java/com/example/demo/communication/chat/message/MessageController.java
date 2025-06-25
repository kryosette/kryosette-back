//package com.example.demo.communication.chat.message;
//
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//@Controller
//public class MessageController {
//    private final SimpMessagingTemplate messagingTemplate;
//    private final MessageService messageService;
//
//    public MessageController(SimpMessagingTemplate messagingTemplate, MessageService messageService) {
//        this.messagingTemplate = messagingTemplate;
//        this.messageService = messageService;
//    }
//
//    @MessageMapping("/chat/{chatId}/send")
//    public void handleMessage(@DestinationVariable Long chatId,
//                              @Payload CreateMessageDto messageDto) {
//        // Сохраняем сообщение в БД
//        MessageDto savedMessage = messageService.createMessage(messageDto);
//
//        // Отправляем сообщение всем подписчикам
//        messagingTemplate.convertAndSend("/topic/messages/" + chatId, savedMessage);
//    }
//}