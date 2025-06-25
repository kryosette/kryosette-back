package com.example.demo.communication.chat.message;

import com.example.demo.communication.chat.room.Room;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Добавляем отношение ManyToOne к Room
    @JoinColumn(name = "room_id") // Указываем столбец для связи
    private Room room;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timeStamp;

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timeStamp = LocalDateTime.now();
    }

    public Message(Room room, String sender, String content) {
        this.room = room;
        this.sender = sender;
        this.content = content;
        this.timeStamp = LocalDateTime.now();
    }
}