package com.substring.chat.applications.dtos.messages.reaction;

import lombok.Data;

@Data
public class ReactionDto {
    private String userId;
    private String reaction;
}