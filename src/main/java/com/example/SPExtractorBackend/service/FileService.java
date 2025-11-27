package com.example.SPExtractorBackend.service;


import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.entity.File;
import com.example.SPExtractorBackend.repository.DriveRepository;
import com.example.SPExtractorBackend.repository.FileRepository;
import com.example.SPExtractorBackend.response.GraphFilesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final RestTemplate restTemplate;
    private final FileRepository fileRepository;
    private final DriveRepository driveRepository;

    private final Set<String> processedFileIds = ConcurrentHashMap.newKeySet();
    private final AtomicInteger totalFilesSaved = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Integer> fileNameCounts = new ConcurrentHashMap<>();


    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    // Dependency Injection
    @Autowired
    public FileService(RestTemplateBuilder restTemplateBuilder, FileRepository fileRepository, DriveRepository driveRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.fileRepository = fileRepository;
        this.driveRepository = driveRepository;
    }

    // Fetch all files for a given driveId. If fresh data exists in the database (updated within 24 hours), return it.
    // Otherwise, retrieve files recursively from Microsoft Graph API and update the database.
    @Cacheable(value = "files", key = "#driveId")
    public List<FileDTO> fetchAllFiles(String bearerToken, String driveId) {
        logger.info("[CACHE MISS] Checking for files in drive: {}", driveId);
        LocalDateTime threshold = LocalDateTime.now().minusHours(24); // Define data freshness threshold

        // Check if recent data exists in the database for files in this drive
        List<File> cachedFiles = fileRepository.findRecentFilesByDriveId(driveId, threshold);
        if (!cachedFiles.isEmpty()) {
            logger.info("[DATABASE HIT] Found {} fresh files in database (updated within 24h) - will be cached in memory", cachedFiles.size());
            List<FileDTO> fileDTOs = cachedFiles.stream()
                    .map(this::mapToFileDTO)
                    .toList();
            logger.info("[SUCCESS] Returning {} cached files from database", fileDTOs.size());
            return fileDTOs;
        }

        logger.info("[DATABASE MISS] No fresh data found in database. Fetching from Microsoft Graph API...");
        processedFileIds.clear(); // Clear the processed IDs set for this drive fetch
        fileNameCounts.clear(); // Clear the filename counts for this drive fetch
        
        List<FileDTO> files = new ArrayList<>();
        totalFilesSaved.set(0); // Reset counter for this drive
        
        // Single pass: collect files that meet size criteria (>= 15MB)
        fetchFilesRecursively(bearerToken, driveId, "/root", files);
        
        logger.info("[API] Fetched {} files (>= 15MB) from Microsoft Graph API", files.size());
        
        // Count filename occurrences for duplicate detection
        for (FileDTO file : files) {
            String fileName = file.getName().toLowerCase();
            fileNameCounts.put(fileName, fileNameCounts.getOrDefault(fileName, 0) + 1);
        }
        
        // Filter based on: >50MB OR >5 years OR duplicate
        long largeFileThreshold = 50L * 1024 * 1024; // 50 MB
        LocalDateTime fiveYearsAgo = LocalDateTime.now().minusYears(5);
        List<FileDTO> filteredFiles = new ArrayList<>();
        
        for (FileDTO file : files) {
            String fileName = file.getName().toLowerCase();
            int nameCount = fileNameCounts.getOrDefault(fileName, 1);
            boolean isDuplicate = nameCount > 1;
            boolean isLarge = file.getSize() > largeFileThreshold;
            boolean isOld = file.getLastModifiedDateTime().isBefore(fiveYearsAgo);
            
            if (isLarge || isOld || isDuplicate) {
                // Add reasons for flagging
                if (isLarge) file.getFlagReasons().add("large");
                if (isOld) file.getFlagReasons().add("old");
                if (isDuplicate) file.getFlagReasons().add("duplicate");
                
                filteredFiles.add(file);
            }
        }
        
        logger.info("[FILTER] {} files match criteria (duplicates: {}, large: {}, old: {})", 
                filteredFiles.size(),
                filteredFiles.stream().filter(f -> fileNameCounts.get(f.getName().toLowerCase()) > 1).count(),
                filteredFiles.stream().filter(f -> f.getSize() > largeFileThreshold).count(),
                filteredFiles.stream().filter(f -> f.getLastModifiedDateTime().isBefore(fiveYearsAgo)).count());
        
        // Save filtered files to database
        if (!filteredFiles.isEmpty()) {
            int savedCount = saveFilesToDatabase(filteredFiles, driveId);
            totalFilesSaved.set(savedCount);
        }

        if (totalFilesSaved.get() > 0) {
            logger.info("[DATABASE] Successfully saved {} total files to database for drive: {}", totalFilesSaved.get(), driveId);
        }
        logger.info("[API SUCCESS] Returning {} filtered files - will be cached in memory", filteredFiles.size());
        return filteredFiles;
    }

    // Fetch files recursively for a specific driveId and folder (itemId) using the Microsoft Graph API.
    // Handles nested folder structures by recursively calling itself for subfolders.
    // Uses pagination to retrieve all files in a folder if results span multiple pages.
    // Only fetches files >= 15MB to reduce API calls
    private void fetchFilesRecursively(String bearerToken, String driveId, String itemId, List<FileDTO> files) {
        String url = graphApiBaseUrl + "/drives/" + driveId + "/items/" + itemId + "/children";
    
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    
        int pageCount = 0;
        while (url != null) {
            pageCount++;
            // Use the helper with retry logic
            ResponseEntity<GraphFilesResponse> response = exchangeWithRetry(url, HttpMethod.GET, requestEntity);
    
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GraphFilesResponse body = response.getBody();
                List<GraphFilesResponse.Item> items = body.getValue();
    
                if (items != null && !items.isEmpty()) {
                    if (pageCount == 1) {
                        logger.info("[PROGRESS] Processing folder with {} items (total files collected: {})", items.size(), files.size());
                    }
                    processItemsInBatches(items, bearerToken, driveId, files);
                }
            
                if (url != null) {
                    logger.debug("[API] Fetching next page: {}", url);
                }
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
    // - Files are included if: size > 50MB OR age > 5 years OR duplicate name exists
    // Adds qualifying files to the result list in a thread-safe manner.
    private void processItem(GraphFilesResponse.Item item, String bearerToken, String driveId, List<FileDTO> files) {
        if (!processedFileIds.add(item.getId())) {
            // Already processed by another thread - but still count the filename for duplicate detection
            if (!item.isFolder()) {
                String fileName = item.getName().toLowerCase();
                fileNameCounts.compute(fileName, (k, v) -> (v == null) ? 1 : v + 1);
            }
            return;
        }

        // Check if the item is a folder or file and process accordingly
        if (item.isFolder()) {
            fetchFilesRecursively(bearerToken, driveId, item.getId(), files);
        }
        // Only include files >= 15MB
        else if (item.getSize() >= 15L * 1024 * 1024) {
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

private int saveFilesToDatabase(List<FileDTO> files, String driveId) {
    if (files.isEmpty()) {
        logger.debug("[WARNING] No files to save to database");
        return 0;
    }

    List<File> entities = files.stream()
            .map(dto -> {
                File entity = mapToFileEntity(dto);
                entity.setLastUpdated(LocalDateTime.now());
                return entity;
            })
            .toList();

    try {
        fileRepository.saveAll(entities);
        return entities.size();
    } catch (DataIntegrityViolationException e) {
        // If batch save fails due to duplicates, save individually and skip duplicates
        logger.debug("[WARNING] Batch save failed, saving files individually to skip duplicates");
        int savedCount = 0;
        for (File entity : entities) {
            try {
                fileRepository.save(entity);
                savedCount++;
            } catch (DataIntegrityViolationException ex) {
                // Skip duplicate, don't log each one
                logger.trace("[SKIP] Duplicate file skipped: {}", entity.getId());
            }
        }
        return savedCount;
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
        logger.info("[DELETE] Deleting file: {} from drive: {}", fileId, driveId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            // Make the DELETE request to Microsoft Graph API
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // If successful, delete the file from the database
                logger.info("[SUCCESS] File successfully deleted from Microsoft Graph API");
                fileRepository.deleteById(fileId);
                logger.info("[SUCCESS] File successfully deleted from database");
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
                logger.warn("[THROTTLED] API rate limit hit. Retrying after {} seconds (attempt {}/{})", sleepTime, attempt + 1, maxRetries);
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

    // Get cached file count for a site from database only (no API calls)
    public long getCachedFileCountForSite(String siteId) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        
        // Get all drives for this site
        List<String> driveIds = driveRepository.findAllBySiteId(siteId)
                .stream()
                .map(drive -> drive.getId())
                .toList();
        
        if (driveIds.isEmpty()) {
            return 0;
        }
        
        // Count files in these drives that are fresh (within 24h)
        return fileRepository.countRecentFilesByDriveIds(driveIds, threshold);
    }


}




