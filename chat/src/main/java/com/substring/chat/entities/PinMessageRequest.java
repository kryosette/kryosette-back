package com.substring.chat.entities;

import lombok.Data;

@Data
public class PinMessageRequest {
    private Long messageId;
    private boolean pin;
}