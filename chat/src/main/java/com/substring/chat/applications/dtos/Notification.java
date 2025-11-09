package com.substring.chat.applications.dtos;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

// Notification.java - сущность уведомления
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipientId; 
    private String senderId;   
    private String content;
    private boolean isRead;
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private Long relatedEntityId; 
}
