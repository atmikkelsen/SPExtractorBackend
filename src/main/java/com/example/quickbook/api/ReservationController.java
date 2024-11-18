package com.example.quickbook.api;

import com.example.quickbook.dto.ReservationRequest;
import com.example.quickbook.dto.ReservationResponse;
import com.example.quickbook.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin
public class ReservationController {
    ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getReservations() {
        return reservationService.getReservations();
    }

    @PostMapping
    public ReservationResponse makeReservation(@RequestBody ReservationRequest reservationRequest){
        return reservationService.makeReservation(reservationRequest);
    }


}
