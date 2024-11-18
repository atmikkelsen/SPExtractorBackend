package com.example.quickbook.config;

import com.example.quickbook.entity.Hotel;
import com.example.quickbook.entity.Room;

import java.util.*;

public class HotelTestDataFactory {
    private static final Random random = new Random();
    private static final int MIN_ROOMS = 10;
    private static final int MAX_ROOMS = 40;
    private static final int MIN_BEDS = 1;
    private static final int MAX_BEDS = 4;
    private static final String[] cityArray = {"New York", "Los Angeles", "Chicago", "Houston", "Phoenix"};
    private static final String[] countryArray = {"USA", "Canada", "Mexico", "France", "Spain"};
    // ... Add more arrays for street names, hotel names, etc.

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
        // Assuming you have an array of hotel names
        return getRandomElement(new String[]{"Grand Plaza", "Ocean View", "Mountain Retreat"}); // Add more hotel names as needed
    }

    public static List<Hotel> generateTestHotels(int numberOfHotels) {
        List<Hotel> hotels = new ArrayList<>();
        for (int i = 0; i < numberOfHotels; i++) {
            Hotel hotel = new Hotel(); // Assuming a constructor is available
            hotel.setName(getRandomHotelName());
            hotel.setCity(getRandomElement(cityArray));
            hotel.setCountry(getRandomElement(countryArray));
            hotel.setStreet(getRandomStreetName());
            hotel.setZip(getRandomZipCode());

            int numberOfRooms = MIN_ROOMS + random.nextInt(MAX_ROOMS - MIN_ROOMS + 1);
            for (int j = 0; j < numberOfRooms; j++) {
                Room room = new Room(); // Assuming a constructor is available
                room.setRoomNumber(j + 1);
                int numberOfBeds = MIN_BEDS + random.nextInt(MAX_BEDS - MIN_BEDS + 1);
                room.setNumberOfBeds(numberOfBeds); // Assuming a setter is available
                room.setPrice(100 * numberOfBeds); // Assuming a setter is available

                hotel.addRoom(room); // Assuming a method to add rooms to a hotel
            }

            hotels.add(hotel);
        }
        return hotels;
    }



    public static void main(String[] args) {
        List<Hotel> testHotels = generateTestHotels(250);
        // Additional code to handle the generated test hotels, like printing
    }
}
