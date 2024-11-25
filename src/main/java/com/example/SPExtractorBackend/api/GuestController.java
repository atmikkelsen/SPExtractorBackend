package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.UserDTO;
import com.example.SPExtractorBackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
@CrossOrigin
public class GuestController {
    /*
    @Autowired
    private UserService userService;

    public GuestController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping
    List<GuestResponse> getGuests() {
        return userService.getAllGuests();
    }

    @GetMapping(path = "/{id}")
    public GuestResponse getGuestById(@PathVariable int id) {
        return userService.getGuestById(id);
    }

    @PostMapping
    public GuestResponse addGuest(@RequestBody UserDTO userDTO) {
        return userService.addGuest(userDTO);
    }

     */

}
