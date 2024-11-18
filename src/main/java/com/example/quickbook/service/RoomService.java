package com.example.quickbook.service;

import com.example.quickbook.dto.RoomResponse;
import com.example.quickbook.entity.Hotel;
import com.example.quickbook.entity.Room;
import com.example.quickbook.repository.HotelRepository;
import com.example.quickbook.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;
    private HotelRepository hotelRepository;

    public RoomService( RoomRepository roomRepository, HotelRepository hotelRepository){
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;

    }

    public List<Room> getRoomsByHotelId(Integer hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }


    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<RoomResponse> getAllRoomsFromHotelId(int hotelId) {
        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        return rooms.stream().map((RoomResponse::new)).toList();
    }

    public RoomResponse addRoom(RoomResponse roomResponse) {
        Room room = Room.builder()
                .hotel((hotelRepository.findById(roomResponse.getHotelId()).orElseThrow()))
                .roomNumber(roomResponse.getRoomNumber())
                .numberOfBeds(roomResponse.getNumberOfBeds())
                .build();
        roomRepository.save(room);
        return new RoomResponse(room);
    }
}
