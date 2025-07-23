package com.substring.chat.entities;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePrivateRoomRequest {
    @NotBlank
    private String participant1Id;

    @NotBlank
    private String participant2Id;

    private String participant1Email;

    private String participant2Email;
}