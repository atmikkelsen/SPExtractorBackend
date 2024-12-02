package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.dto.LargeFileDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileService {

    public List<LargeFileDTO> getFilesBySite(String siteId) {
        // Simuleret data - kald evt. Graph API her
        return List.of(
                new LargeFileDTO("File1.docx", 1024, "Documents", "https://example.com/file1", "Owner1", LocalDateTime.now()),
                new LargeFileDTO("File2.pdf", 2048, "Shared", "https://example.com/file2", "Owner2", LocalDateTime.now())
        );
    }


}
