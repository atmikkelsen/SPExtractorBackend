package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.SiteDTO;
import com.example.SPExtractorBackend.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/sites")
@CrossOrigin(origins = "https://localhost:3000", allowedHeaders = {"Authorization", "Content-Type"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class SiteController {

    private final SiteService siteService;

    @Autowired
    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public ResponseEntity<Object> getAllSites(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Authorization header is missing or invalid"));
            }

            String token = authorizationHeader.substring(7);

            List<SiteDTO> sites = siteService.fetchAllSites(token);
            System.out.printf("All sites fetched successfully from Microsoft Graph API%n");
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

    @GetMapping("/{siteId}")
    public ResponseEntity<Object> getSiteById(@PathVariable String siteId, @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("Authorization header is missing or invalid");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Authorization header is missing or invalid"));
        }

        try {
            String token = authorizationHeader.substring(7);

            // Pass token to the service
            SiteDTO site = siteService.fetchSiteById(token, siteId);
            System.out.printf("Site fetched successfully from Microsoft Graph API%n");
            return ResponseEntity.ok(site);

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
}