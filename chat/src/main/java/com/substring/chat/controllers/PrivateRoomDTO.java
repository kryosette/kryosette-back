package com.substring.chat.controllers;

import com.substring.chat.entities.RoomType;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PrivateRoomDTO {
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private String createdBy;
    private RoomType type;
    private List<String> participantIds;
}