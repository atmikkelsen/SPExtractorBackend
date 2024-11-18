package com.example.quickbook.service;

import com.example.quickbook.dto.HotelRequest;
import com.example.quickbook.dto.HotelResponse;
import com.example.quickbook.entity.Hotel;
import com.example.quickbook.entity.Room;
import com.example.quickbook.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {
    @Autowired
    private HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository){
        this.hotelRepository = hotelRepository;
    }

    public List<HotelResponse> getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();
        return hotels.stream().map((HotelResponse::new)).toList();
    }

    public HotelResponse addHotel(HotelRequest hotelRequest) {
        Hotel hotel = Hotel.builder()
                .name(hotelRequest.getName())
                .street(hotelRequest.getStreet())
                .city(hotelRequest.getCity())
                .zip(hotelRequest.getZip())
                .country(hotelRequest.getCountry())
                .build();
        hotelRepository.save(hotel);
        return new HotelResponse(hotel);
    }

    public HotelResponse getHotelById(int id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        return new HotelResponse(hotel);
    }

    public void deleteHotelById(int id) {
        hotelRepository.deleteById(id);
    }

    public HotelResponse updateHotelById(int id, HotelRequest hotelRequest) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        hotel.setName(hotelRequest.getName());
        hotel.setStreet(hotelRequest.getStreet());
        hotel.setCity(hotelRequest.getCity());
        hotel.setZip(hotelRequest.getZip());
        hotel.setCountry(hotelRequest.getCountry());
        hotelRepository.save(hotel);
        return new HotelResponse(hotel);
    }

}
