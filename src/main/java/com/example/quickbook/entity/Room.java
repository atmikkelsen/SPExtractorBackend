package com.example.quickbook.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = "hotel")
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // or another strategy as per your DB requirements
    private int id;
    private int roomNumber;
    private int numberOfBeds;
    private int price;
    @CreationTimestamp
    private LocalDateTime created;
    @UpdateTimestamp
    private LocalDateTime updated;

    @ManyToOne()
    private Hotel hotel;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)

    private List<Reservation> reservations = new ArrayList<>();

}
