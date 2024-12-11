package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(
        origins = "https://localhost:3000",
        allowedHeaders = {"Authorization", "Content-Type"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping()
    public ResponseEntity<Object> getFilesByDrive(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String driveId) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.emptyList());
            }
            String token = authorizationHeader.substring(7);

            List<FileDTO> files = fileService.fetchAllFiles(token, driveId);
            System.out.printf("Files fetched successfully from Microsoft Graph API%n");
            return ResponseEntity.ok(files);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Token expired or invalid"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "An unexpected error occurred"));
            }
        }
    }

}


