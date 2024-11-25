package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.TestUtils;
import com.example.SPExtractorBackend.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class SiteServiceTest {
    SiteService siteService;
    @Autowired
    SiteRepository siteRepository;

    private boolean dataInitialized = false;
/*
    @BeforeEach
    void setUp() {
        siteService = new SiteService(siteRepository);
        if(!dataInitialized) {
            siteRepository.deleteAll();
            TestUtils.setupTestHotels(siteRepository);
            dataInitialized = true;
        }
    }

    //Test der tilføjer et site til databasen




    @Test
    void getHotelByID() {
        HotelResponse hotel = siteService.getHotelById(1);
        assertEquals(1, hotel.getId());
    }

    @Test
    void getHotels() {
        List<HotelResponse> hotels = siteService.getAllHotels();
        assertEquals(250, hotels.size());
    }

 */

}
