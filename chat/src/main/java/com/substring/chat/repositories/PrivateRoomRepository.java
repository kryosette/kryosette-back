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
    @Query("SELECT pr FROM PrivateRoom pr " +
            "WHERE (SELECT COUNT(p) FROM pr.participants p WHERE p.id.userId IN (:userId1, :userId2)) = 2 " +
            "AND (SELECT COUNT(p) FROM pr.participants p) = 2")
    Optional<PrivateRoom> findPrivateRoomBetweenUsers(@Param("userId1") String userId1,
                                                      @Param("userId2") String userId2);
}