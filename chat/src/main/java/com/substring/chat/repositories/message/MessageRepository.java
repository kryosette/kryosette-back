package com.substring.chat.repositories.message;

import com.substring.chat.domain.model.messages.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Поиск сообщений по комнате
    List<Message> findByRoomId(Long roomId);
    List<Message> findByRoomIdOrderByTimestampAsc(Long roomId);
    // Поиск последних сообщений
    List<Message> findTop10ByRoomIdOrderByTimestampDesc(Long roomId);
}