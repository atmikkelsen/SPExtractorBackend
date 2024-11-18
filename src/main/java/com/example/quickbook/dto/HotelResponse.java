package com.example.quickbook.dto;

import com.example.quickbook.entity.Hotel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class HotelResponse {
    private Integer id;
    private String name;
    private String address;
    private int numberOfRooms;

    public HotelResponse(Hotel h) {
        this.id = h.getId();
        this.name = h.getName();
        this.address = h.getAddress();
        if (h.getRooms() != null){
            this.numberOfRooms = h.getRooms().size();
        } else {
            this.numberOfRooms = 0;
        }
    }

}
