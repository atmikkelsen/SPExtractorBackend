package com.example.SPExtractorBackend.repository;

import com.example.SPExtractorBackend.entity.Drive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Drive, Integer> {

}
