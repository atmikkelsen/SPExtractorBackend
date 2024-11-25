package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.dto.FileDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GraphApiService {

    // Simuleret API-kald til Graph
    public List<FileDTO> fetchFilesForSite(Long siteId) {
        // Eksempel på data hentet fra Microsoft Graph API
        // Her kan du bruge en HTTP-klient som RestTemplate eller WebClient
        List<FileDTO> files = new ArrayList<>();

        // Simuler data (erstattes med API-kald)
        files.add(new FileDTO("File1.docx", 2, "Documents", "https://sharepoint.com/file1",
                LocalDateTime.now(), "John Doe", 1));
        files.add(new FileDTO("File2.pdf", 2, "Shared", "https://sharepoint.com/file2",
                LocalDateTime.now(), "Jane Smith", 1));

        return files;
    }
}
