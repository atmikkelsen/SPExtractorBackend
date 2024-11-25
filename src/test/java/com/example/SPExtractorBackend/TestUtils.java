package com.example.SPExtractorBackend;

import com.example.SPExtractorBackend.config.HotelTestDataFactory;
import com.example.SPExtractorBackend.repository.SiteRepository;

public class TestUtils {
    public static void setupTestHotels(SiteRepository siteRepository) {
        HotelTestDataFactory hotelTestDataFactory = new HotelTestDataFactory();
        siteRepository.saveAll(HotelTestDataFactory.generateTestHotels(250));


    }
}
