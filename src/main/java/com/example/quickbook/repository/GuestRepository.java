package com.example.quickbook.repository;

import com.example.quickbook.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Integer> {

    Guest findGuestById(int guestId);
}
