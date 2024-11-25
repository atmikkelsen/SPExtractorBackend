package com.example.SPExtractorBackend.config;

import com.example.SPExtractorBackend.entity.File;
import com.example.SPExtractorBackend.entity.Site;

import java.util.*;
/*
public class HotelTestDataFactory {

    private static final Random random = new Random();
    private static final int MIN_ROOMS = 10;
    private static final int MAX_ROOMS = 40;
    private static final int MIN_BEDS = 1;
    private static final int MAX_BEDS = 4;
    private static final String[] cityArray = {"New York", "Los Angeles", "Chicago", "Houston", "Phoenix"};
    private static final String[] countryArray = {"USA", "Canada", "Mexico", "France", "Spain"};
    // ... Add more arrays for street names, site names, etc.

    private static String getRandomElement(String[] array) {
        return array[random.nextInt(array.length)];
    }

    private static String getRandomZipCode() {
        return String.format("%04d", random.nextInt(100000));
    }

    private static String getRandomStreetName() {
        // Assuming you have an array of street names
        return getRandomElement(new String[]{"Main St", "Second St", "Third St"}); // Add more street names as needed
    }

    private static String getRandomHotelName() {
        // Assuming you have an array of site names
        return getRandomElement(new String[]{"Grand Plaza", "Ocean View", "Mountain Retreat"}); // Add more site names as needed
    }

    public static List<Site> generateTestHotels(int numberOfHotels) {
        List<Site> sites = new ArrayList<>();
        for (int i = 0; i < numberOfHotels; i++) {
            Site site = new Site(); // Assuming a constructor is available
            site.setName(getRandomHotelName());
            site.setCity(getRandomElement(cityArray));
            site.setCountry(getRandomElement(countryArray));
            site.setStreet(getRandomStreetName());
            site.setZip(getRandomZipCode());

            int numberOfRooms = MIN_ROOMS + random.nextInt(MAX_ROOMS - MIN_ROOMS + 1);
            for (int j = 0; j < numberOfRooms; j++) {
                File file = new File(); // Assuming a constructor is available
                file.setRoomNumber(j + 1);
                int numberOfBeds = MIN_BEDS + random.nextInt(MAX_BEDS - MIN_BEDS + 1);
                file.setNumberOfBeds(numberOfBeds); // Assuming a setter is available
                file.setPrice(100 * numberOfBeds); // Assuming a setter is available

                site.addRoom(file); // Assuming a method to add files to a site
            }

            sites.add(site);
        }
        return sites;
    }



    public static void main(String[] args) {
        List<Site> testSites = generateTestHotels(250);
        // Additional code to handle the generated test hotels, like printing
    }
}

 */
