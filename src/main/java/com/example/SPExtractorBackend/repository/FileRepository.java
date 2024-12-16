package com.example.SPExtractorBackend.repository;

import com.example.SPExtractorBackend.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {
    boolean existsByNameAndDriveId(String name, String driveId);

    @Query("SELECT f FROM File f WHERE f.driveId = :driveId AND f.lastUpdated > :threshold")
    List<File> findRecentFilesByDriveId(String driveId, LocalDateTime threshold);

    List<File> findAllByDriveId(String driveId);
    File findByNameAndDriveId(String name, String driveId);
    void deleteByNameAndDriveId(String name, String driveId);




}
