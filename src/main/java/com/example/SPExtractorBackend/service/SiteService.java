package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.dto.DriveDTO;
import com.example.SPExtractorBackend.dto.LargeFileDTO;
import com.example.SPExtractorBackend.dto.SiteDTO;
import com.example.SPExtractorBackend.response.GraphDrivesResponse;
import com.example.SPExtractorBackend.response.GraphItemsResponse;
import com.example.SPExtractorBackend.response.GraphSitesResponse;
import com.microsoft.graph.models.Drive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteService {
    private final RestTemplate restTemplate;

    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    @Autowired
    public SiteService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    public List<SiteDTO> fetchAllSites(String bearerToken) {
        String url = graphApiBaseUrl + "/sites?search=*";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<GraphSitesResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, GraphSitesResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {

            return response.getBody().getValue().stream()
                    .map(site -> new SiteDTO(site.getId(), site.getName(), site.getWebUrl(), site.getDisplayName()))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Failed to fetch sites from Microsoft Graph API");
        }
    }

    public SiteDTO fetchSiteById(String bearerToken, String siteId) {
        String url = graphApiBaseUrl + "/sites/" + siteId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<SiteDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, SiteDTO.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            System.out.println(response.getBody().getName());
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to fetch site from Microsoft Graph API");
        }
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
}
