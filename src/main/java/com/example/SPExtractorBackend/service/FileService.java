package com.example.SPExtractorBackend.service;


import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.response.GraphFilesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

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
        String url = graphApiBaseUrl + "/drives/" + driveId + "/root/children";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<GraphFilesResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, GraphFilesResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            System.out.println("Files fetched successfully from Microsoft Graph API"
                    + response.getBody().getValue().size() + " files");
            return response.getBody().getValue().stream()
                    .map(file -> new FileDTO(
                            file.getName(),
                            file.getSize(),
                            file.getWebUrl(),
                            file.getLastModifiedDateTime()))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Failed to fetch files from Microsoft Graph API");
        }
    }


}
