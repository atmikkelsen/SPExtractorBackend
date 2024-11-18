package com.example.quickbook.repository;

import com.example.quickbook.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByHotelId(Integer hotelId);

    Room findRoomById(int roomId);
}
