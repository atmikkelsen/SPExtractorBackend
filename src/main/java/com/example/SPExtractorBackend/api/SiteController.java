package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.SiteDTO;
import com.example.SPExtractorBackend.entity.File;
import com.example.SPExtractorBackend.service.SiteService;
import com.example.SPExtractorBackend.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/sites")
@CrossOrigin(origins = "http://127.0.0.1:5500") // Adjust based on frontend URL
public class SiteController {

    private final SiteService siteService;

    @Autowired
    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public ResponseEntity<List<SiteDTO>> getAllSites() {
        try {
            List<SiteDTO> sites = siteService.fetchAllSites();
            System.out.printf("Sites fetched successfully from Microsoft Graph API%n");

            return ResponseEntity.ok(sites);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
}
