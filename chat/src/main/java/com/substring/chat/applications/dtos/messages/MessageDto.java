package com.substring.chat.applications.dtos.messages;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String content;
    private String text;
    private String sender;
    private String userId;
    private Long roomId;
    private Instant timestamp;
}
