package com.substring.chat.applications.dtos.private_room;

import com.substring.chat.applications.dtos.room.RoomType;
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