package com.substring.chat.applications.dtos;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String senderUsername;
    private String content;
    private boolean isRead;
    private Instant createdAt;
    private NotificationType type;
    private Long relatedEntityId;
}
