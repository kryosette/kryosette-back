//package com.substring.chat.config;
//
//import com.substring.chat.controllers.MessageDto;
//import com.substring.chat.entities.ReactionDto;
//import com.substring.chat.repositories.PrivateRoomRepository;
//import com.substring.chat.repositories.RoomRepository;
//import com.substring.chat.services.MessageService;
//import com.substring.chat.services.PrivateMessageService;
//import com.substring.chat.services.TypingStatusDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.messaging.handler.annotation.*;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.nio.file.AccessDeniedException;
//import java.security.Principal;
//
//@Controller
//@RequiredArgsConstructor
//public class ReactionWebSocketController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final PrivateMessageService privateMessageService;
//    private final PrivateRoomRepository privateRoomRepository;
//
//    @MessageMapping("/message/{messageId}/react")
//    public void handleReaction(
//            @DestinationVariable Long messageId,
//            @Payload ReactionDto reactionDto,
//            Principal principal) {
//
//        // Получаем roomId из сообщения (можно добавить в ReactionDto)
//        Long roomId = privateMessageService.getRoomIdByMessageId(messageId);
//
//        // Проверяем доступ пользователя к комнате
//        if (!isUserInRoom(principal.getName(), roomId)) {
//            throw new AccessDeniedException("User not in room");
//        }
//
//        // Добавляем/удаляем реакцию через существующий сервис
//        if (reactionDto.getAction() == ReactionAction.ADD) {
//            privateMessageService.addReaction(
//                    roomId,
//                    messageId,
//                    reactionDto.getReaction(),
//                    "Bearer " + principal.getName()
//            );
//        } else {
//            privateMessageService.removeReaction(roomId, messageId, reactionDto.getReaction());
//        }
//
//        // Получаем обновленные реакции
//        Map<String, Long> reactions = privateMessageService.getReactions(messageId);
//
//        // Рассылаем обновление всем подписанным клиентам
//        messagingTemplate.convertAndSend(
//                "/topic/message/" + messageId + "/reactions",
//                reactions
//        );
//    }
//
//    private boolean isUserInRoom(String userId, Long roomId) {
//        return privateRoomRepository.findById(roomId)
//                .map(room -> room.getParticipants().stream()
//                        .anyMatch(p -> p.getUserId().equals(userId)))
//                .orElse(false);
//    }
//}