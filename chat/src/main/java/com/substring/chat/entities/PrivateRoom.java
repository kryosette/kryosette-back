package com.substring.chat.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "private_rooms")
@Data
public class PrivateRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoomParticipant> participants = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private RoomType type = RoomType.PRIVATE;

    @PrePersist
    protected void onCreate() {
        if (name == null) {
            this.name = "Chat-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    public void addParticipant(String userId) {
        participants.add(new RoomParticipant(this, userId));
    }

    public boolean isParticipant(String userId) {
        return participants.stream()
                .anyMatch(p -> p.getUserId().equals(userId));
    }
}