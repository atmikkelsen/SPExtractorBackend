package com.example.quickbook.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

}
