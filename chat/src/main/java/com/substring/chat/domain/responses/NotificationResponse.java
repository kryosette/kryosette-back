package com.substring.chat.domain.responses;

import lombok.Data;
import lombok.NonNull;

@Data
public class NotificationResponse {
    @NonNull
    private String message;
    private String sender;
}
