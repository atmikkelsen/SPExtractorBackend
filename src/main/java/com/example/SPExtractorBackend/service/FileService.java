package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.entity.File;
import com.example.SPExtractorBackend.repository.SiteRepository;
import com.example.SPExtractorBackend.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileService {

    public List<FileDTO> getFilesBySite(Long siteId) {
        // Simuleret data - kald evt. Graph API her
        return List.of(
                new FileDTO("File1.docx", 1024, "Documents", "https://example.com/file1", LocalDateTime.now(), "Owner1", 1),
                new FileDTO("File2.pdf", 2048, "Shared", "https://example.com/file2", LocalDateTime.now(), "Owner2", 2)
        );
    }
}

