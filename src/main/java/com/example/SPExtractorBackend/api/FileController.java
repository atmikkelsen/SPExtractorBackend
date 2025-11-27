package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(
        origins = "https://localhost:8443",
        allowedHeaders = {"Authorization", "Content-Type"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // This method is used to get all files by drive ID
    @GetMapping()
    public ResponseEntity<Object> getFilesByDrive(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String driveId) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.emptyList());
            }
            String token = authorizationHeader.substring(7);

            List<FileDTO> files = fileService.fetchAllFiles(token, driveId);
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

    // This method is used to delete a file by its ID
    @DeleteMapping("/{driveId}/items/{fileId}")
    public ResponseEntity<Object> deleteFile(
            @PathVariable String driveId,
            @PathVariable String fileId,
            @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Authorization header is missing or invalid"));
        }

        String token = authorizationHeader.substring(7);

        try {
            fileService.deleteFile(token, driveId, fileId);
            return ResponseEntity.noContent().build(); // No content on successful delete
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }


}


