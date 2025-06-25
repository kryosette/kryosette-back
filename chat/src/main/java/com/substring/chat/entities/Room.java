package com.substring.chat.entities;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    //  @GeneratedValue(strategy = GenerationType.IDENTITY)  Удаляем auto-generation
    private String id = UUID.randomUUID().toString();  // Генерируем UUID в коде
    private String roomId;
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Message> messages = new ArrayList<>();

    @PrePersist  // Генерируем UUID перед сохранением в базу данных
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}