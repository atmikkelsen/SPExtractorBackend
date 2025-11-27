package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.dto.DriveDTO;
import com.example.SPExtractorBackend.dto.SiteDTO;
import com.example.SPExtractorBackend.response.GraphDrivesResponse;
import com.example.SPExtractorBackend.response.GraphSitesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteService {
    private static final Logger logger = LoggerFactory.getLogger(SiteService.class);
    private final RestTemplate restTemplate;

    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    @Autowired
    public SiteService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    // Fetch all sites from Microsoft Graph API using the provided bearer token
    @Cacheable(value = "sites", key = "'allSites'")
    public List<SiteDTO> fetchAllSites(String bearerToken) {
        logger.info("[CACHE MISS] Fetching all sites from Microsoft Graph API");
        String url = graphApiBaseUrl + "/sites?search=*";

        // Set the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request to Microsoft Graph API to fetch all sites
        ResponseEntity<GraphSitesResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, GraphSitesResponse.class);

        // If the response is successful and the body is not null, map the response to SiteDTO
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<SiteDTO> sites = response.getBody().getValue().stream()
                    .map(site -> new SiteDTO(site.getId(), site.getName(), site.getWebUrl(), site.getDisplayName()))
                    .collect(Collectors.toList());
            logger.info("[API SUCCESS] Fetched {} sites from Microsoft Graph API - will be cached", sites.size());
            return sites;
        } else {
            throw new RuntimeException("Failed to fetch sites from Microsoft Graph API");
        }
    }

    // Fetch a specific site by siteId
    @Cacheable(value = "sites", key = "#siteId")
    public SiteDTO fetchSiteById(String bearerToken, String siteId) {
        logger.info("[CACHE MISS] Fetching site by ID: {} from Microsoft Graph API", siteId);
        String url = graphApiBaseUrl + "/sites/" + siteId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<SiteDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, SiteDTO.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            SiteDTO site = response.getBody();
            logger.info("[API SUCCESS] Fetched site: {} (ID: {}) - will be cached", site.getName(), siteId);
            return site;
        } else {
            throw new RuntimeException("Failed to fetch site from Microsoft Graph API");
        }
    }

}
