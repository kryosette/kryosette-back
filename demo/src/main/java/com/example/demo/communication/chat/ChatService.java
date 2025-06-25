//package com.example.demo.communication.chat;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ChatService {
//    private final ChatRepository chatRepository;
//
//    public ChatDto createChat(CreateChatDto chatDto, String username) {
//        Chat chat = new Chat();
//        chat.setName(chatDto.getName());
//        chat.setCreatedAt(LocalDateTime.now());
//        // Здесь можно добавить создателя чата, если нужно
//        // chat.setCreatedBy(username);
//
//        Chat savedChat = chatRepository.save(chat);
//        return mapChatToDto(savedChat);
//    }
//
//    public List<ChatDto> getAllChats() {
//        return chatRepository.findAll().stream()
//                .map(this::mapChatToDto)
//                .collect(Collectors.toList());
//    }
//
//    public ChatDto getChatById(Long id) {
//        return chatRepository.findById(id)
//                .map(this::mapChatToDto)
//                .orElseThrow(() -> new RuntimeException("Chat not found"));
//    }
//
//    private ChatDto mapChatToDto(Chat chat) {
//        ChatDto dto = new ChatDto();
//        dto.setId(chat.getId());
//        dto.setName(chat.getName());
//        dto.setCreatedAt(chat.getCreatedAt());
//        // Здесь можно добавить дополнительные поля, например:
//        // dto.setCreatedBy(chat.getCreatedBy());
//        return dto;
//    }
//}
