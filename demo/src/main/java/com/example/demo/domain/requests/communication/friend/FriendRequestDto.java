package com.example.demo.domain.requests.communication.friend;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FriendRequestDto {
    private Long id;
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private String receiverUsername;
    private String status;
    private LocalDateTime createdAt;
}