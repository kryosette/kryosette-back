package com.substring.chat.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMessageDto {
    @NotBlank
    private String content;
    private Long roomId;
}