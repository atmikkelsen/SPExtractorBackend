package com.example.SPExtractorBackend.service;


import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.entity.File;
import com.example.SPExtractorBackend.repository.FileRepository;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileService {
    private final RestTemplate restTemplate;
    private final FileRepository fileRepository;

    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    @Autowired
    public FileService(RestTemplateBuilder restTemplateBuilder, FileRepository fileRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.fileRepository = fileRepository;
    }

    public List<FileDTO> fetchAllFiles(String bearerToken, String driveId) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24); // Define data freshness threshold

        // Check if recent data exists in the database
        List<File> cachedFiles = fileRepository.findRecentFilesByDriveId(driveId, threshold);
        if (!cachedFiles.isEmpty()) {
            System.out.println("Returning cached files from database.");
            return cachedFiles.stream()
                    .map(this::mapToFileDTO)
                    .toList();
        }

        System.out.println("No fresh data found. Fetching from Microsoft Graph API...");
        // Fetch fresh data from the Microsoft Graph API
        List<FileDTO> files = new ArrayList<>();
        fetchFilesRecursively(bearerToken, driveId, "/root", files);

        // Save fetched data to the database
        saveFilesToDatabase(files, driveId);

        return files;
    }

    private void fetchFilesRecursively(String bearerToken, String driveId, String itemId, List<FileDTO> files) {
        String url = graphApiBaseUrl + "/drives/" + driveId + "/items/" + itemId + "/children";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Loop for pagination
        while (url != null) {
            ResponseEntity<GraphFilesResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, GraphFilesResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GraphFilesResponse body = response.getBody();
                List<GraphFilesResponse.Item> items = body.getValue();

                if (items != null && !items.isEmpty()) {
                    processItemsInBatches(items, bearerToken, driveId, files);
                }

                url = body.getNextLink(); // Pagination link
            } else {
                throw new RuntimeException("Failed to fetch files from Microsoft Graph API for item: " + itemId);
            }
        }
    }

    private void processItemsInBatches(List<GraphFilesResponse.Item> items, String bearerToken, String driveId, List<FileDTO> files) {
        final int batchSize = 10;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < items.size(); i += batchSize) {
            int fromIndex = i;
            int toIndex = Math.min(i + batchSize, items.size());

            futures.add(CompletableFuture.runAsync(() -> {
                List<GraphFilesResponse.Item> batch = items.subList(fromIndex, toIndex);
                for (GraphFilesResponse.Item item : batch) {
                    if (!fileRepository.existsByNameAndDriveId(item.getName(), driveId)) {
                        processItem(item, bearerToken, driveId, files);
                    }
                }
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
    }

    private void processItem(GraphFilesResponse.Item item, String bearerToken, String driveId, List<FileDTO> files) {
        if (item.isFolder()) {
            fetchFilesRecursively(bearerToken, driveId, item.getId(), files);
        } else if (item.getSize() > 15 * 1024 * 1024 ||
                item.getLastModifiedDateTime().isBefore(LocalDateTime.now().minusDays(1095))) {
            FileDTO fileDTO = new FileDTO(
                    item.getName(),
                    item.getSize(),
                    item.getWebUrl(),
                    item.getLastModifiedDateTime(),
                    item.getLastModifiedBy().getUser().getDisplayName(),
                    driveId
            );
            synchronized (files) {
                files.add(fileDTO);
            }
        }
    }

    private void saveFilesToDatabase(List<FileDTO> files, String driveId) {
        System.out.println("Saving files to the database...");

        List<File> entities = files.stream()
                .map(dto -> {
                    File entity = mapToFileEntity(dto);
                    entity.setLastUpdated(LocalDateTime.now());
                    return entity;
                })
                .toList();

        fileRepository.saveAll(entities);
        System.out.println("Files saved successfully to the database.");
    }

    private File mapToFileEntity(FileDTO dto) {
        File entity = new File();
        entity.setName(dto.getName());
        entity.setSize(dto.getSize());
        entity.setWebUrl(dto.getWebUrl());
        entity.setLastModifiedDateTime(dto.getLastModifiedDateTime());
        entity.setLastModifiedByDisplayName(dto.getLastModifiedByDisplayName());
        entity.setDriveId(dto.getDriveId());
        return entity;
    }

    private FileDTO mapToFileDTO(File entity) {
        return new FileDTO(
                entity.getName(),
                entity.getSize(),
                entity.getWebUrl(),
                entity.getLastModifiedDateTime(),
                entity.getLastModifiedByDisplayName(),
                entity.getDriveId()
        );
    }
}


