package com.substring.chat.repositories;


import com.substring.chat.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


// RoomRepository.java
public interface RoomRepository extends JpaRepository<Room, Long> {
    // Удален метод findByRoomId - используем стандартный findById
    // Добавим поиск по имени если нужно
    Optional<Room> findByName(String name);
}