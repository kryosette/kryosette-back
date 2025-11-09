package com.substring.chat.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(name = "message_reactions")
@IdClass(MessageReactionId.class)
@Data
public class MessageReaction {
    @Id
    @ManyToOne
    @JoinColumn(name = "message_id")
    private PrivateMessage message;

    @Id
    private String reaction;
}

class MessageReactionId implements Serializable {
    private Long message;
    private String reaction;
}