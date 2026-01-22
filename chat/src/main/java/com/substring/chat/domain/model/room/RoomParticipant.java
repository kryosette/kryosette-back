package com.substring.chat.domain.model.room;

import com.substring.chat.domain.model.room.private_room.PrivateRoom;
import jakarta.persistence.*;
import lombok.*;

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

