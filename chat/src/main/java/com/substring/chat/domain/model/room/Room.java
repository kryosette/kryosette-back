package com.substring.chat.domain.model.room;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

// Room.java
@Entity
@Table(name = "rooms")
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "user_id")
    private String userId;

}
