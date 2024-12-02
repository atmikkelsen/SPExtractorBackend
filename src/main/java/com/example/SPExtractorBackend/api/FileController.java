package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.LargeFileDTO;
import com.example.SPExtractorBackend.service.FileService;
import com.example.SPExtractorBackend.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class FileController {

    @Autowired
    private FileService fileService;

    private SiteService siteService;

    @Value("${bearer.token}")
    private String BEARER_TOKEN;

    @GetMapping("/{siteId}")
    public List<LargeFileDTO> getFilesBySite(@PathVariable String siteId) {
        System.out.println("Fetching files for site: " + siteId);
        return fileService.getFilesBySite(siteId);
    }
}


