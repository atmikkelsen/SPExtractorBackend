package com.example.quickbook.api;

import com.example.quickbook.dto.GuestRequest;
import com.example.quickbook.dto.GuestResponse;
import com.example.quickbook.service.GuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
@CrossOrigin
public class GuestController {
    @Autowired
    private GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }
    @GetMapping
    List<GuestResponse> getGuests() {
        return guestService.getAllGuests();
    }

    @GetMapping(path = "/{id}")
    public GuestResponse getGuestById(@PathVariable int id) {
        return guestService.getGuestById(id);
    }

    @PostMapping
    public GuestResponse addGuest(@RequestBody GuestRequest guestRequest) {
        return guestService.addGuest(guestRequest);
    }

}
