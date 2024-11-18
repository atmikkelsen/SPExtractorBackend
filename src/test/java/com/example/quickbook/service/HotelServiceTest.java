package com.example.quickbook.service;

import com.example.quickbook.TestUtils;
import com.example.quickbook.dto.HotelResponse;
import com.example.quickbook.repository.HotelRepository;
import com.example.quickbook.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class HotelServiceTest {
    HotelService hotelService;
    @Autowired
    HotelRepository hotelRepository;

    private boolean dataInitialized = false;

    @BeforeEach
    void setUp() {
        hotelService = new HotelService(hotelRepository);
        if(!dataInitialized) {
            hotelRepository.deleteAll();
            TestUtils.setupTestHotels(hotelRepository);
            dataInitialized = true;
        }
    }

    //Test der tilføjer et hotel til databasen




    @Test
    void getHotelByID() {
        HotelResponse hotel = hotelService.getHotelById(1);
        assertEquals(1, hotel.getId());
    }

    @Test
    void getHotels() {
        List<HotelResponse> hotels = hotelService.getAllHotels();
        assertEquals(250, hotels.size());
    }

}
