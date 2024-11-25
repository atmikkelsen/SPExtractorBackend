package com.example.SPExtractorBackend.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    /*
    @Autowired
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<GuestResponse> getAllGuests() {
        List<AppUser> users = userRepository.findAll();
        return users.stream().map((GuestResponse::new)).toList();
    }


    public GuestResponse addGuest(UserDTO userDTO) {
        AppUser user = AppUser.builder()
                .username(userDTO.getUsername())
                .firstname(userDTO.getFirstName())
                .lastname(userDTO.getLastName())
                .email(userDTO.getEmail())
                .phoneNumber(userDTO.getPhoneNumber())
                .build();
        userRepository.save(user);
        return new GuestResponse(user);
    }

    public GuestResponse getGuestById(int id) {
        AppUser user = userRepository.findById(id).orElseThrow();
        return new GuestResponse(user);
    }

     */

}
