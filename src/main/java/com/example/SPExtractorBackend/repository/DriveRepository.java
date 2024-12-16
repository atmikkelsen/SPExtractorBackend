package com.example.SPExtractorBackend.repository;

import com.example.SPExtractorBackend.entity.Drive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriveRepository extends JpaRepository<Drive, String> {
    List<Drive> findAllBySiteId(String siteId);
}
