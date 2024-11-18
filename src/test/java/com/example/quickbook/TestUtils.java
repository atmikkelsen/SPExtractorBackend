package com.example.quickbook;

import com.example.quickbook.config.HotelTestDataFactory;
import com.example.quickbook.repository.HotelRepository;

public class TestUtils {
    public static void setupTestHotels(HotelRepository hotelRepository) {
        HotelTestDataFactory hotelTestDataFactory = new HotelTestDataFactory();
        hotelRepository.saveAll(HotelTestDataFactory.generateTestHotels(250));


    }
}
