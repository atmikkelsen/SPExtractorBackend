package com.example.quickbook.dto;

import com.example.quickbook.entity.Hotel;
import com.example.quickbook.entity.Room;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class RoomResponse {
    private int id;
    private int roomNumber;
    private int numberOfBeds;
    private int hotelId;
    private int price;

    public RoomResponse(Room r) {
        this.id = r.getId();
        this.roomNumber = r.getRoomNumber();
        this.numberOfBeds = r.getNumberOfBeds();
        this.hotelId = r.getHotel().getId();
        this.price = r.getPrice();

    }

}
