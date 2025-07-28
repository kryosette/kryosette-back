package com.substring.chat.entities;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class PrivateMessageDto {
    private Long id;
    private String content;
    private String sender;
    private Long privateRoomId;
    private String userId;
    private Instant timestamp;
    private Map<String, Long> reactions;
}
