package com.example.demo.communication.chat.members;

import com.example.demo.common.BaseEntity;
import com.example.demo.communication.chat.room.Room;
import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
public class ChatMembers extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room roomId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private User memberId;

}
