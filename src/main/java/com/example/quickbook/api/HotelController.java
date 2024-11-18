package com.example.quickbook.api;

import com.example.quickbook.dto.HotelRequest;
import com.example.quickbook.dto.HotelResponse;
import com.example.quickbook.entity.Room;
import com.example.quickbook.service.HotelService;
import com.example.quickbook.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class HotelController {
    @Autowired
    private HotelService hotelService;
    private RoomService roomService;
    public HotelController(HotelService hotelService, RoomService roomService){
        this.hotelService = hotelService;
        this.roomService = roomService;
    }

    @GetMapping
    public List<HotelResponse> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping(path = "/{id}")
    public HotelResponse getHotelById(@PathVariable int id) {
        return hotelService.getHotelById(id);
    }

    @PutMapping(path = "/{id}")
    HotelResponse updateHotelById(@PathVariable int id, @RequestBody HotelRequest hotelRequest) {
        return hotelService.updateHotelById(id, hotelRequest);
    }

    @GetMapping(path = "delete/{id}")
    public void deleteHotelById(@PathVariable int id) {
        hotelService.deleteHotelById(id);
    }

    @PostMapping
    public HotelResponse addHotel(@RequestBody HotelRequest hotelRequest) {
        return hotelService.addHotel(hotelRequest);
    }

    @GetMapping(path = "/{hotelId}/rooms")
    public ResponseEntity<List<Room>> getHotelRooms(@PathVariable Integer hotelId) {
        List<Room> rooms = roomService.getRoomsByHotelId(hotelId);
        return ResponseEntity.ok(rooms);
    }

    @DeleteMapping(path = "/{Id}")
    public void deleteHotel(@PathVariable int Id) {
        hotelService.deleteHotelById(Id);
    }




}
