package com.example.demo.communication.friend;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FriendDto {
    private String id;
    private String username;
    private String avatarUrl;
    private LocalDateTime friendsSince;
}