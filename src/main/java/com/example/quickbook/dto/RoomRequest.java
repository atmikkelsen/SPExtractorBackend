package com.example.quickbook.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequest {
    private String roomNumber;
    private String numberOfBeds;
    private String hotelId;
    private int price;
}
