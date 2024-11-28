package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.dto.SiteDTO;
import com.example.SPExtractorBackend.entity.Site;
import com.example.SPExtractorBackend.repository.SiteRepository;
import com.example.SPExtractorBackend.response.GraphSitesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    // Hardcoded Bearer Token (Replace this with your actual token)
    @Value("${bearer.token}")
    private String BEARER_TOKEN;

    public List<SiteDTO> fetchAllSites() {
        String url = graphApiBaseUrl + "/sites?search=*";

        // Set the Authorization header with the hardcoded token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(BEARER_TOKEN); // Add Bearer token
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
}
