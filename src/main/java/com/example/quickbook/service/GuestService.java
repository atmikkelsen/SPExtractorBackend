package com.example.quickbook.service;

import com.example.quickbook.dto.GuestRequest;
import com.example.quickbook.dto.GuestResponse;
import com.example.quickbook.entity.Guest;
import com.example.quickbook.repository.GuestRepository;
import com.example.quickbook.entity.Guest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestService {
    @Autowired
    private GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public List<GuestResponse> getAllGuests() {
        List<Guest> guests = guestRepository.findAll();
        return guests.stream().map((GuestResponse::new)).toList();
    }


    public GuestResponse addGuest(GuestRequest guestRequest) {
        Guest guest = Guest.builder()
                .username(guestRequest.getUsername())
                .firstname(guestRequest.getFirstName())
                .lastname(guestRequest.getLastName())
                .email(guestRequest.getEmail())
                .phoneNumber(guestRequest.getPhoneNumber())
                .build();
        guestRepository.save(guest);
        return new GuestResponse(guest);
    }

    public GuestResponse getGuestById(int id) {
        Guest guest = guestRepository.findById(id).orElseThrow();
        return new GuestResponse(guest);
    }

}
