package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.dto.DriveDTO;
import com.example.SPExtractorBackend.response.GraphDrivesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriveService {
    private final RestTemplate restTemplate;

    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    @Autowired
    public DriveService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    public List<DriveDTO> fetchAllDrives(String bearerToken, String siteId) {
        String url = graphApiBaseUrl + "/sites/" + siteId + "/drives";


        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<GraphDrivesResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, GraphDrivesResponse.class);

        System.out.println(response.getBody());

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {

            return response.getBody().getValue().stream()
                    .map(drive -> new DriveDTO(drive.getId(), drive.getName(), drive.getWebUrl(), drive.getLastModifiedDateTime()))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Failed to fetch drives from Microsoft Graph API");
        }
    }

    public DriveDTO fetchDriveById(String bearerToken, String driveId) {
        String url = graphApiBaseUrl + "/drives/" + driveId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<DriveDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, DriveDTO.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            System.out.println(response.getBody().getName());
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to fetch drive from Microsoft Graph API");
        }
    }




}
