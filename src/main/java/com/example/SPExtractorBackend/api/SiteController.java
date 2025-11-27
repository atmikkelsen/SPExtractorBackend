package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.SiteDTO;
import com.example.SPExtractorBackend.service.FileService;
import com.example.SPExtractorBackend.service.SiteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/sites")
@CrossOrigin(origins = "https://localhost:8443", allowedHeaders = {"Authorization", "Content-Type"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class SiteController {

    private final SiteService siteService;
    private final FileService fileService;

    public SiteController(SiteService siteService, FileService fileService) {
        this.siteService = siteService;
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<Object> getAllSites(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                // Return a 400 Bad Request response if the Authorization header is missing or invalid
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Authorization header is missing or invalid"));
            }
            // Extract token from the Authorization header
            String token = authorizationHeader.substring(7);

            List<SiteDTO> sites = siteService.fetchAllSites(token);
            return ResponseEntity.ok(sites);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                System.out.println("Token expired or invalid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Token expired or invalid"));
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                System.out.println("Access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            } else {
                System.out.println("Unexpected error: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An unexpected error occurred"));
            }
        }
    }

    // This method is used to get a specific site by its ID
    @GetMapping("/{siteId}")
    public ResponseEntity<Object> getSiteById(@PathVariable String siteId, @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Authorization header is missing or invalid"));
        }

        try {
            String token = authorizationHeader.substring(7);

            SiteDTO site = siteService.fetchSiteById(token, siteId);
            return ResponseEntity.ok(site);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Token expired or invalid"));
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An unexpected error occurred"));
            }
        }
    }

    // Get cached file count for a site (from database only, no API calls)
    @GetMapping("/{siteId}/cached-file-count")
    public ResponseEntity<Object> getCachedFileCount(@PathVariable String siteId) {
        try {
            long count = fileService.getCachedFileCountForSite(siteId);
            return ResponseEntity.ok(Collections.singletonMap("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to retrieve cached file count"));
        }
    }
}