package com.example.SPExtractorBackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

}
