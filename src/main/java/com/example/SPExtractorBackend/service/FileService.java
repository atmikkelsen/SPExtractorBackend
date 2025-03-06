package com.example.SPExtractorBackend.service;


import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.entity.File;
import com.example.SPExtractorBackend.repository.FileRepository;
import com.example.SPExtractorBackend.response.GraphFilesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cglib.core.Local;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileService {
    private final RestTemplate restTemplate;
    private final FileRepository fileRepository;

    private final Set<String> processedFileIds = ConcurrentHashMap.newKeySet();


    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    // Dependency Injection
    @Autowired
    public FileService(RestTemplateBuilder restTemplateBuilder, FileRepository fileRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.fileRepository = fileRepository;
    }

    // Fetch all files for a given driveId. If fresh data exists in the database (updated within 24 hours), return it.
    // Otherwise, retrieve files recursively from Microsoft Graph API and update the database.
    public List<FileDTO> fetchAllFiles(String bearerToken, String driveId) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24); // Define data freshness threshold

        // Check if recent data exists in the database for files in this drive
        List<File> cachedFiles = fileRepository.findRecentFilesByDriveId(driveId, threshold);
        if (!cachedFiles.isEmpty()) {
            System.out.println("Returning cached files from database.");
            return cachedFiles.stream()
                    .map(this::mapToFileDTO)
                    .toList();
        }

        System.out.println("No fresh data found. Fetching from Microsoft Graph API...");
        List<FileDTO> files = new ArrayList<>();
        fetchFilesRecursively(bearerToken, driveId, "/root", files);

        // Save fetched data to the database
        saveFilesToDatabase(files, driveId);
        return files;
    }

    // Fetch files recursively for a specific driveId and folder (itemId) using the Microsoft Graph API.
    // Handles nested folder structures by recursively calling itself for subfolders.
    // Uses pagination to retrieve all files in a folder if results span multiple pages.
    private void fetchFilesRecursively(String bearerToken, String driveId, String itemId, List<FileDTO> files) {
        String url = graphApiBaseUrl + "/drives/" + driveId + "/items/" + itemId + "/children";
    
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    
        while (url != null) {
            // Use the helper with retry logic
            ResponseEntity<GraphFilesResponse> response = exchangeWithRetry(url, HttpMethod.GET, requestEntity);
    
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GraphFilesResponse body = response.getBody();
                List<GraphFilesResponse.Item> items = body.getValue();
    
                if (items != null && !items.isEmpty()) {
                    processItemsInBatches(items, bearerToken, driveId, files);

                    // Optionally, save the current batch immediately:
                    saveFilesToDatabase(new ArrayList<>(files), driveId);
                    // Clear the in-memory list if you’re only using it as a temporary batch holder
                    synchronized (files) {
                        files.clear();
                    }
                }
            
                System.out.println("Fetching URL: " + url);
                url = body.getNextLink();
            } else {
                throw new RuntimeException("Failed to fetch files from Microsoft Graph API for item: " + itemId);
            }
        }
    }

    // Process a list of file items in batches of a fixed size to improve performance.
    // Leverages a thread pool to process batches asynchronously, reducing overall processing time for large datasets.
    private void processItemsInBatches(List<GraphFilesResponse.Item> items, String bearerToken, String driveId, List<FileDTO> files) {
        final int batchSize = 10;
        // Create a thread pool with 5 threads
        ExecutorService executor = Executors.newFixedThreadPool(5);
        // List to hold all CompletableFuture tasks
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Process items in batches asynchronously
        for (int i = 0; i < items.size(); i += batchSize) {
            int fromIndex = i;
            int toIndex = Math.min(i + batchSize, items.size());

            // Add a CompletableFuture task to the list for each batch of items
            futures.add(CompletableFuture.runAsync(() -> {
                List<GraphFilesResponse.Item> batch = items.subList(fromIndex, toIndex);
                // Process each item in the batch
                for (GraphFilesResponse.Item item : batch) {
                    // Check if the file already exists in the database before processing
                    if (!fileRepository.existsByNameAndDriveId(item.getName(), driveId)) {
                        processItem(item, bearerToken, driveId, files);
                    }
                }
            }, executor));
        }
        // Wait for all CompletableFuture tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
    }

    // Process a single file or folder item based on specific criteria:
    // - Folders trigger a recursive fetch.
    // - Files larger than 15MB or older than 1 year are included in the result list.
    // Adds qualifying files to the result list in a thread-safe manner.
    private void processItem(GraphFilesResponse.Item item, String bearerToken, String driveId, List<FileDTO> files) {
        if (!processedFileIds.add(item.getId())) {
            // Already processed by another thread
            return;
        }
        int fileSizeToProcessInMB = 15 * 1024 * 1024;
        LocalDateTime fileAgeToProcessInDays = LocalDateTime.now().minusDays(3650);

        // Check if the item is a folder or file and process accordingly
        if (item.isFolder()) {
            fetchFilesRecursively(bearerToken, driveId, item.getId(), files);
        }
        // Check if the file meets the criteria to be processed and add it to the list
        else if (item.getSize() > fileSizeToProcessInMB ||
                item.getLastModifiedDateTime().isBefore(fileAgeToProcessInDays)) {
            FileDTO fileDTO = new FileDTO(
                    item.getId(),
                    item.getName(),
                    item.getSize(),
                    item.getWebUrl(),
                    item.getLastModifiedDateTime(),
                    item.getLastModifiedBy().getUser().getDisplayName(),
                    driveId
            );
            // Add the file to the list in a thread-safe manner
            synchronized (files) {
                files.add(fileDTO);
            }
        }
    }

private void saveFilesToDatabase(List<FileDTO> files, String driveId) {
    // System.out.println("Saving files to the database...");

    List<File> entities = files.stream()
            .map(dto -> {
                File entity = mapToFileEntity(dto);
                entity.setLastUpdated(LocalDateTime.now());
                return entity;
            })
            .toList();

    try {
        fileRepository.saveAll(entities);
        // System.out.println("Files saved successfully to the database.");
    } catch (DataIntegrityViolationException e) {
        System.err.println("Duplicate entry detected: " + e.getMessage());
    }
}

    // Helper methods to map FileDTO to File entity
    private File mapToFileEntity(FileDTO dto) {
        File entity = new File();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setSize(dto.getSize());
        entity.setWebUrl(dto.getWebUrl());
        entity.setLastModifiedDateTime(dto.getLastModifiedDateTime());
        entity.setLastModifiedByDisplayName(dto.getLastModifiedByDisplayName());
        entity.setDriveId(dto.getDriveId());
        return entity;
    }

    // Helper method to map File entity to FileDTO
    private FileDTO mapToFileDTO(File entity) {
        return new FileDTO(
                entity.getId(),
                entity.getName(),
                entity.getSize(),
                entity.getWebUrl(),
                entity.getLastModifiedDateTime(),
                entity.getLastModifiedByDisplayName(),
                entity.getDriveId()
        );
    }

    // Delete a file from Microsoft Graph API and the database
    public void deleteFile(String bearerToken, String driveId, String fileId) {
        String url = graphApiBaseUrl + "/drives/" + driveId + "/items/" + fileId;
        System.out.println("Deleting file with URL: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            // Make the DELETE request to Microsoft Graph API
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // If successful, delete the file from the database
                System.out.println("File successfully deleted from Microsoft Graph. Now removing from the database...");
                fileRepository.deleteById(fileId);
                System.out.println("File successfully deleted from the database.");
            } else {
                throw new RuntimeException("Failed to delete file. HTTP Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file: " + e.getMessage(), e);
        }
    }



    private ResponseEntity<GraphFilesResponse> exchangeWithRetry(String url, HttpMethod method, HttpEntity<?> requestEntity) {
    int maxRetries = 5;
    int attempt = 0;
    while (attempt < maxRetries) {
        try {
            return restTemplate.exchange(url, method, requestEntity, GraphFilesResponse.class);
        } catch (HttpClientErrorException e) {
            // Check for HTTP 429 (Too Many Requests)
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                String retryAfterValue = e.getResponseHeaders().getFirst("Retry-After");
                int sleepTime = retryAfterValue != null ? Integer.parseInt(retryAfterValue) : (int) Math.pow(2, attempt);
                System.out.println("Throttled by Graph API. Retrying after " + sleepTime + " seconds (attempt " + (attempt + 1) + ")...");
                try {
                    Thread.sleep(sleepTime * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry backoff", ie);
                }
                attempt++;
            } else {
                // Rethrow if it’s any other error
                throw e;
            }
        }
    }
    throw new RuntimeException("Exceeded max retries for URL: " + url);
}




}


