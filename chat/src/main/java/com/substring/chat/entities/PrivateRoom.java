package com.substring.chat.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "private_rooms")
@Data
@Getter
@Setter
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

    @ElementCollection
    @CollectionTable(name = "blocked_users", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "email_id")
    private Set<String> blockedUsers = new HashSet<>();

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

    public void blockUser(String emailId) {
        blockedUsers.add(emailId);
    }

    public void unblockUser(String emailId) {
        blockedUsers.remove(emailId);
    }

    public boolean isUserBlocked(String emailId) {
        return blockedUsers.contains(emailId);
    }

    @ManyToOne
    @JoinColumn(name = "pinned_message_id")
    private PrivateMessage pinnedMessage;
}