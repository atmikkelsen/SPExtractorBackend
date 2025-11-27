package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.dto.DriveDTO;
import com.example.SPExtractorBackend.entity.Drive;
import com.example.SPExtractorBackend.repository.DriveRepository;
import com.example.SPExtractorBackend.response.GraphDrivesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class DriveService {
    private static final Logger logger = LoggerFactory.getLogger(DriveService.class);

    private final RestTemplate restTemplate;
    private final DriveRepository driveRepository;

    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    // Dependency Injection
    public DriveService(RestTemplateBuilder restTemplateBuilder, DriveRepository driveRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.driveRepository = driveRepository;
    }

    /**
     * Fetch all drives for a given siteId.
     * Caches the drives using Spring Cache and also persists to database.
     */
    @Cacheable(value = "drives", key = "#siteId")
    public List<DriveDTO> fetchAllDrives(String bearerToken, String siteId, String siteName) {
        logger.info("[CACHE MISS] Fetching drives for site: {} ({})", siteName, siteId);

        // Check if drives for this site are already in database
        List<Drive> cachedDrives = driveRepository.findAllBySiteId(siteId);
        if (!cachedDrives.isEmpty()) {
            logger.info("[DATABASE HIT] Found {} drives in database for site: {}", cachedDrives.size(), siteName);
            List<DriveDTO> driveDTOs = cachedDrives.stream()
                    .map(this::mapToDriveDTO)
                    .collect(Collectors.toList());
            logger.info("[SUCCESS] Returning {} cached drives from database - will be cached in memory", driveDTOs.size());
            return driveDTOs;
        }

        logger.info("[DATABASE MISS] No drives found in database, fetching from Microsoft Graph API...");

        String url = graphApiBaseUrl + "/sites/" + siteId + "/drives";

        // Set the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request to Microsoft Graph API to fetch drives for the site with siteId
        ResponseEntity<GraphDrivesResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, GraphDrivesResponse.class);

        // Check if the response is successful and has data and map the response to DriveDTO
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<DriveDTO> drives = response.getBody().getValue().stream()
                    .map(drive -> new DriveDTO(
                            drive.getId(),
                            drive.getName(),
                            drive.getWebUrl(),
                            drive.getLastModifiedDateTime(),
                            siteId,
                            siteName // Persist the site name along with the drive as it is not available in the drive response
                    ))
                    .collect(Collectors.toList());

            // Save drives to the database
            logger.info("[DATABASE] Saving {} drives to database for site: {}", drives.size(), siteName);
            saveDrivesToDatabase(drives);
            logger.info("[API SUCCESS] Fetched and cached {} drives - will be cached in memory", drives.size());

            return drives;
        } else {
            throw new RuntimeException("Failed to fetch drives from Microsoft Graph API");
        }
    }

    /**
     * Fetch a single drive by driveId.
     */
    @Cacheable(value = "drives", key = "'drive-' + #driveId")
    public DriveDTO fetchDriveById(String bearerToken, String driveId) {
        logger.info("[CACHE MISS] Fetching drive by ID: {}", driveId);
        
        // Check if the drive exists in the database and return it if found
        Drive cachedDrive = driveRepository.findById(driveId).orElse(null);
        if (cachedDrive != null) {
            logger.info("[DATABASE HIT] Found drive in database: {} - will be cached in memory", cachedDrive.getName());
            return mapToDriveDTO(cachedDrive);
        }

        logger.info("[DATABASE MISS] Drive not in database, fetching from Microsoft Graph API...");
        // Fetch fresh data from Graph API if not cached
        String url = graphApiBaseUrl + "/drives/" + driveId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request to Microsoft Graph API to fetch the drive by driveId
        ResponseEntity<DriveDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, DriveDTO.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            DriveDTO driveDTO = response.getBody();

            // Save the drive to the database
            Drive entity = mapToDriveEntity(driveDTO);
            driveRepository.save(entity);
            logger.info("[API SUCCESS] Fetched and saved drive: {} - will be cached in memory", driveDTO.getName());

            return driveDTO;
        } else {
            throw new RuntimeException("Failed to fetch drive from Microsoft Graph API");
        }
    }

    // Helper method to save drives to the database
    private void saveDrivesToDatabase(List<DriveDTO> drives) {
        List<Drive> entities = drives.stream()
                .map(this::mapToDriveEntity)
                .collect(Collectors.toList());
        driveRepository.saveAll(entities);
        logger.info("[DATABASE] Successfully saved {} drives to database", drives.size());
    }

    // Helper methods to map DriveDTO to Drive entity
    private Drive mapToDriveEntity(DriveDTO dto) {
        Drive entity = new Drive();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setWebUrl(dto.getWebUrl());
        entity.setLastModifiedDateTime(dto.getLastModifiedDateTime());
        entity.setSiteId(dto.getSiteId());
        entity.setSiteName(dto.getSiteName());
        return entity;
    }

    // Helper method to map Drive entity to DriveDTO
    private DriveDTO mapToDriveDTO(Drive entity) {
        return new DriveDTO(
                entity.getId(),
                entity.getName(),
                entity.getWebUrl(),
                entity.getLastModifiedDateTime(),
                entity.getSiteId(),
                entity.getSiteName()
        );
    }
}

