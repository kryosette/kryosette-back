//package com.example.demo.communication.chat.message;
//
//import com.example.demo.communication.chat.Chat;
//import com.example.demo.communication.chat.ChatRepository;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class MessageService {
//    private final MessageRepository messageRepository;
//    private final ChatRepository chatRepository;
//
//    public MessageDto createMessage(CreateMessageDto messageDto) {
//        Chat chat = chatRepository.findById(messageDto.getChatId())
//                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
//
//
//        Message message = new Message();
//        message.setContent(messageDto.getContent());
//        message.setSender(messageDto.getSender());
//        message.setChat(chat);
//        message.setTimestamp(LocalDateTime.now());
//
//        Message saved = messageRepository.save(message);
//        return mapToDto(saved);
//    }
//
//    public List<MessageDto> getMessagesByChatId(Long chatId) {
//        return messageRepository.findByChatId(chatId).stream()
//                .map(this::mapToDto)
//                .collect(Collectors.toList());
//    }
//
//    private MessageDto mapToDto(Message message) {
//        MessageDto dto = new MessageDto();
//        dto.setId(message.getId());
//        dto.setContent(message.getContent());
//        dto.setTimestamp(message.getTimestamp());
//        return dto;
//    }
//}