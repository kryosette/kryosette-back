package com.example.demo.communication.chat.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RoomRepository extends JpaRepository<Room, String> {
    //get room using room id
    Optional<Room> findByRoomId(String roomId);
}

