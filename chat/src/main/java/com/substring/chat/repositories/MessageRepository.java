package com.substring.chat.repositories;

import com.substring.chat.entities.Message;
import com.substring.chat.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Поиск сообщений по комнате
    List<Message> findByRoomId(Long roomId);
    List<Message> findByRoomIdOrderByTimestampAsc(Long roomId);
    // Поиск последних сообщений
    List<Message> findTop10ByRoomIdOrderByTimestampDesc(Long roomId);
}