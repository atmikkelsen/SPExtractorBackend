package com.example.quickbook.api;

import com.example.quickbook.dto.RoomResponse;
import com.example.quickbook.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class RoomController {
    @Autowired
    RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping(path = "/{hotelId}")
    public List<RoomResponse> getAllRoomsFromHotelId(@PathVariable int hotelId) {
        return roomService.getAllRoomsFromHotelId(hotelId);
    }

    @PostMapping(path = "/{hotelId}")
    public RoomResponse addRoom(@RequestBody RoomResponse roomResponse) {
        return roomService.addRoom(roomResponse);
    }
}
