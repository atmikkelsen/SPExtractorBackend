package com.example.SPExtractorBackend.repository;

import com.example.SPExtractorBackend.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, String> {
    @Query("SELECT s FROM Site s WHERE s.lastUpdated > :threshold")
    List<Site> findRecentSites(LocalDateTime threshold);
}

