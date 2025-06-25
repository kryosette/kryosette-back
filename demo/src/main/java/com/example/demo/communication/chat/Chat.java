//package com.example.demo.communication.chat;
//
//import com.example.demo.communication.chat.message.Message;
//import com.example.demo.user.User;
//import lombok.*;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Chat {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String name;
//    private LocalDateTime createdAt;
//
//    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
//    private List<Message> messages;
//
//    private String chatLink;
//
//    @ManyToMany
//    @JoinTable(
//            name = "chat_participants",
//            joinColumns = @JoinColumn(name = "chat_id"),
//            inverseJoinColumns = @JoinColumn(name = "user_id")
//    )
//    private List<User> participants = new ArrayList<>();
//}