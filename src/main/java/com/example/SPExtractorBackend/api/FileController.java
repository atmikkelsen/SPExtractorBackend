package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://127.0.0.1:5500") // Tilpasses efter frontend URL
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/{siteId}")
    public List<FileDTO> getFilesBySite(@PathVariable Long siteId) {
        return fileService.getFilesBySite(siteId); // Sørg for, at FileService matcher
    }
}


