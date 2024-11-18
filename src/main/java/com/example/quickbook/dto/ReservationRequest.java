package com.example.quickbook.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequest {

        private int guestId;
        private int roomId;
        private String reservationDate;

}
