package com.substring.chat.domain.model.messages.typing;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TypingStatus {
    private String userId;
    private String username;
    private boolean isTyping;
    private long lastUpdate;
}
