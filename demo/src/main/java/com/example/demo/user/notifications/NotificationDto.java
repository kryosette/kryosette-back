package com.example.demo.user.notifications;

import com.example.demo.user.UserDto;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
public class NotificationDto {
    private final String id;
    private final UserDto sender;
    private final NotificationType type;
    private final String message;
    private final boolean isRead;
    private final LocalDateTime createdAt;
}
