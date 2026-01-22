package com.substring.chat.applications.dtos.messages.typing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingStatusDto {
    private String userId;
    private String username;
    private boolean isTyping;
}