package com.example.quickbook.service;

import com.example.quickbook.dto.ReservationRequest;
import com.example.quickbook.dto.ReservationResponse;
import com.example.quickbook.entity.Guest;
import com.example.quickbook.entity.Reservation;
import com.example.quickbook.entity.Room;
import com.example.quickbook.repository.GuestRepository;
import com.example.quickbook.repository.ReservationRepository;
import com.example.quickbook.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    private GuestRepository guestRepository;
    private RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository, GuestRepository guestRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
    }

    public List<ReservationResponse> getReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(ReservationResponse::new).collect(Collectors.toList());

    }

    public ReservationResponse makeReservation(ReservationRequest reservationRequest) {

        if (reservationRequest.getReservationDate() == null) {
            throw new IllegalArgumentException("Reservation date string cannot be null");
        }

        LocalDateTime reservationDate;
        try {
            reservationDate = LocalDateTime.parse(reservationRequest.getReservationDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid reservation date format", e);
        }

        Guest guest = guestRepository.findGuestById(reservationRequest.getGuestId());
        if (guest == null) {
            throw new IllegalArgumentException("Guest not found");
        }

        Room room = roomRepository.findRoomById(reservationRequest.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }

        Reservation reservation = new Reservation();
        reservation.setReservationDate(reservationDate);
        reservation.setGuest(guest);
        reservation.setRoom(room);

        reservationRepository.save(reservation);

        return new ReservationResponse(reservation);
    }
}
