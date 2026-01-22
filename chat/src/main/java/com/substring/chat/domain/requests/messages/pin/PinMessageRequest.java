package com.substring.chat.domain.requests.messages.pin;

import lombok.Data;

@Data
public class PinMessageRequest {
    private Long messageId;
    private boolean pin;
}