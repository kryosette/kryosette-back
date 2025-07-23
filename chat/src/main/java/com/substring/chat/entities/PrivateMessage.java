package com.substring.chat.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "private_messages")
@Data
public class PrivateMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private String sender;
    private String userId;
    private Instant timestamp = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "private_room_id")
    private PrivateRoom privateRoom;
}