package com.example.SPExtractorBackend.repository;

import com.example.SPExtractorBackend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Integer> {

}
