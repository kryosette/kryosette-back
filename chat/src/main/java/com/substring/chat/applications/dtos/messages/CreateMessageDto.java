package com.substring.chat.applications.dtos.messages;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMessageDto {
    @NotBlank
    private String content;
    private Long roomId;
}