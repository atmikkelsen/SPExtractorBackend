package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.DriveDTO;
import com.example.SPExtractorBackend.service.DriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/drives")
@CrossOrigin(origins = "https://localhost:3000", allowedHeaders = {"Authorization", "Content-Type"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class DriveController {

    private final DriveService driveService;


    @Autowired
    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    // This method is used to get all drives
    @GetMapping
    public ResponseEntity<Object> getAllDrives(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String siteId,
            @RequestParam String siteName) {

        System.out.println("sitename::" + siteName);
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
            }
            String token = authorizationHeader.substring(7);

            List<DriveDTO> drives = driveService.fetchAllDrives(token, siteId, siteName);
            System.out.printf("All drives fetched successfully from Microsoft Graph API%n");
            return ResponseEntity.ok(drives);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Token expired or invalid"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An unexpected error occurred"));
            }
        }
    }

    // This method is used to get a specific drive by its ID
    @GetMapping("/{driveId}")
    public ResponseEntity<DriveDTO> getDriveById(@PathVariable String driveId, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            String token = authorizationHeader.substring(7);

            DriveDTO drive = driveService.fetchDriveById(token, driveId);
            System.out.printf("Specific drive fetched successfully from Microsoft Graph API%n");
            return ResponseEntity.ok(drive);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
    }
}
