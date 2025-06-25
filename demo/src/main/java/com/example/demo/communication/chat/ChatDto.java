package com.example.demo.communication.chat;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}