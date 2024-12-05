package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.DriveDTO;
import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.service.FileService;
import com.example.SPExtractorBackend.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(
        origins = "http://127.0.0.1:5500",
        allowedHeaders = {"Authorization", "Content-Type"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class FileController {

    private final FileService fileService;
    @Value("${bearer.token}")
    private String BEARER_TOKEN;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
    @GetMapping()
    public ResponseEntity<List<FileDTO>> getFilesByDrive(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String driveId) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.emptyList());
            }
            String token = authorizationHeader.substring(7);

            List<FileDTO> files = fileService.fetchAllFiles(token, driveId);
            System.out.printf("Files fetched successfully from Microsoft Graph API%n");
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/no-header")
    public ResponseEntity<List<FileDTO>> getFilesBySite() {
        String driveId = "b!dzpGCI90WUm5rtVuQNPFHaHVsN3kQyJOpoL8QYAUxW_9lh1UUzLoQ5TJbOnt2zmK";
        String token = BEARER_TOKEN;

        System.out.println("Fetching files for site: " + driveId);
        List<FileDTO> files = fileService.fetchAllFiles(BEARER_TOKEN, driveId);
        System.out.printf("Files fetched successfully from Microsoft Graph API without header %n");
        return ResponseEntity.ok(files);
    }
}


