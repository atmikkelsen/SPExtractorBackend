package com.example.quickbook.repository;

import com.example.quickbook.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {


}
