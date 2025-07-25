package com.substring.chat.entities;

import java.time.Instant;
import java.util.*;

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
