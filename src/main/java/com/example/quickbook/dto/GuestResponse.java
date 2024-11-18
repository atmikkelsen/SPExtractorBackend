package com.example.quickbook.dto;

import com.example.quickbook.entity.Guest;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class GuestResponse {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    public GuestResponse(Guest g) {
        this.id = g.getId();
        this.username = g.getUsername();
        this.firstName = g.getFirstname();
        this.lastName = g.getLastname();
        this.email = g.getEmail();
        this.phoneNumber = g.getPhoneNumber();
    }



}
