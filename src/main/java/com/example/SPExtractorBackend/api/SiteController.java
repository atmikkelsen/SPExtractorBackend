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
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class SiteController {

    private final SiteService siteService;

    @Autowired
    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public ResponseEntity<List<SiteDTO>> getAllSites(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.emptyList());
            }
            String token = authorizationHeader.substring(7);

            // Pass token to the service
            List<SiteDTO> sites = siteService.fetchAllSites(token);
            System.out.printf("Sites fetched successfully from Microsoft Graph API%n");

            return ResponseEntity.ok(sites);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
}
