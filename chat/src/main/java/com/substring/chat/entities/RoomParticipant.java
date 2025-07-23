package com.substring.chat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "room_participants")
@Getter
@Setter
@NoArgsConstructor
public class RoomParticipant {

    @EmbeddedId
    private RoomParticipantId id;

    @ManyToOne
    @MapsId("roomId") // Связываем с частью составного ключа
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private PrivateRoom room;

    public RoomParticipant(PrivateRoom room, String userId) {
        this.id = new RoomParticipantId(room.getId(), userId);
        this.room = room;
    }

    public String getUserId() {
        return this.id.getUserId();
    }
}

