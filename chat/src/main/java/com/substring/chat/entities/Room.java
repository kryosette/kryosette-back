package com.substring.chat.entities;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

// Room.java
@Entity
@Table(name = "rooms")
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "user_id")
    private String userId;

}
