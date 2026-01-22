package com.substring.chat.domain.model.user.block;

import com.substring.chat.domain.model.room.private_room.PrivateRoom;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "user_blocks")
@Data
public class UserBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocking_user_id")
    private String blockingUserId;

    @Column(name = "blocked_user_id")
    private String blockedUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "private_room_id")
    private PrivateRoom privateRoom;

    private Instant createdAt = Instant.now();
}