package com.example.quickbook.config;

import com.example.quickbook.entity.Hotel;
import com.example.quickbook.repository.GuestRepository;
import com.example.quickbook.repository.HotelRepository;
import com.example.quickbook.repository.ReservationRepository;
import com.example.quickbook.repository.RoomRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DeveloperData implements ApplicationRunner {
    GuestRepository guestRepository;
    HotelRepository hotelRepository;
    ReservationRepository reservationRepository;
    RoomRepository roomRepository;

    public DeveloperData(GuestRepository guestRepository, HotelRepository hotelRepository, ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.guestRepository = guestRepository;
        this.hotelRepository = hotelRepository;
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Hotel> hotels = HotelTestDataFactory.generateTestHotels(250);
        hotelRepository.saveAll(hotels);

        System.out.println("Developer data created " + hotels.size() + " hotels");

    }

}
