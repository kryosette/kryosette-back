package com.substring.chat.repositories;


import com.substring.chat.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoomRepository extends JpaRepository<Room, String> {
    //get room using room id
    Room findByRoomId(String roomId);
}
