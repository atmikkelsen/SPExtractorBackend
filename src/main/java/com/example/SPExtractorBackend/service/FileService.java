package com.example.SPExtractorBackend.service;


import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.response.GraphFilesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    private final RestTemplate restTemplate;

    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    @Autowired
    public FileService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    public List<FileDTO> fetchAllFiles(String bearerToken, String driveId) {
        List<FileDTO> files = new ArrayList<>();
        fetchFilesRecursively(bearerToken, driveId, "/root", files);
        return files;
    }
    private void fetchFilesRecursively(String bearerToken, String driveId, String itemId, List<FileDTO> files) {
        String url = graphApiBaseUrl + "/drives/" + driveId + "/items/" + itemId + "/children";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<GraphFilesResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, GraphFilesResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<GraphFilesResponse.Item> items = response.getBody().getValue();
            if (items != null) {
                System.out.println("Items: " + items.size());
                for (GraphFilesResponse.Item item : items) {
                    System.out.println("Item: " + item.getName()  + " Size: " + item.getSize()/ 1024/ 1024 + " MB");
                    if (item.isFolder()) {
                        System.out.println("Folder: " + item.getName());
                        // It's a folder, recurse into it
                        fetchFilesRecursively(bearerToken, driveId, item.getId(), files);
                    } else if (item.getSize() > 15 *1024 * 1024 || item.getLastModifiedDateTime().isAfter(LocalDateTime.now().minusDays(30))) {
                        // It's a file, and size is > 2 MB
                        files.add(new FileDTO(
                                item.getName(),
                                item.getSize(),
                                item.getWebUrl(),
                                item.getLastModifiedDateTime()
                        ));
                    }
                }
            } else {
                System.out.println("No items found in the response.");
            }
        } else {
            throw new RuntimeException("Failed to fetch files from Microsoft Graph API for item: " + itemId);
        }
    }



}
