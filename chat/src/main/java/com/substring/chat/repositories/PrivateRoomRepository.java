package com.substring.chat.repositories;

import com.substring.chat.entities.PrivateRoom;
import com.substring.chat.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrivateRoomRepository extends JpaRepository<PrivateRoom, Long> {

    @Query("SELECT DISTINCT pr FROM PrivateRoom pr JOIN pr.participants p WHERE p.id.userId = :userId")
    List<PrivateRoom> findRoomsByUserId(@Param("userId") String userId);
    @Query("SELECT DISTINCT pr FROM PrivateRoom pr " +
            "JOIN pr.participants p1 " +
            "JOIN pr.participants p2 " +
            "WHERE p1.id.userId = :userId1 " +
            "AND p2.id.userId = :userId2 " +
            "AND SIZE(pr.participants) = 2")
    Optional<PrivateRoom> findPrivateRoomBetweenUsers(@Param("userId1") String userId1,
                                                      @Param("userId2") String userId2);
}