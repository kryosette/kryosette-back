package com.substring.chat.repositories;

import com.substring.chat.entities.PrivateMessage;
import com.substring.chat.entities.PrivateRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    List<PrivateMessage> findByPrivateRoomIdOrderByTimestampAsc(Long privateRoomId);

    @Query("SELECT pm FROM PrivateMessage pm " +
            "WHERE pm.privateRoom.id = :privateRoomId " +
            "AND pm.id > :lastMessageId " +
            "ORDER BY pm.timestamp ASC")
    List<PrivateMessage> findNewMessages(@Param("privateRoomId") Long privateRoomId,
                                         @Param("lastMessageId") Long lastMessageId);
}