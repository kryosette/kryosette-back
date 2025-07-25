package com.substring.chat.entities;

import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class PrivateMessageDto {
    private Long id;
    private String content;
    private String sender;
    private Long privateRoomId;
    private String userId;
    private Instant timestamp;
    private String reactions;
}
