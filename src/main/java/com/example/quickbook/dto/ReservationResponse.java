package com.example.quickbook.dto;

import com.example.quickbook.entity.Reservation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservationResponse {
    int id;
    String guestUserName;
    int roomId;
    String reservationDate;

    public ReservationResponse(Reservation reservation){
        this.id = reservation.getId();
        this.guestUserName = reservation.getGuest().getUsername();
        this.roomId = reservation.getRoom().getId();
        this.reservationDate = reservation.getReservationDate().toString();
    }

}
