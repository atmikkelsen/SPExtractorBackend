package com.example.SPExtractorBackend.service;


import com.example.SPExtractorBackend.dto.FileDTO;
import com.example.SPExtractorBackend.entity.File;
import com.example.SPExtractorBackend.repository.DriveRepository;
import com.example.SPExtractorBackend.repository.FileRepository;
import com.example.SPExtractorBackend.response.GraphFilesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
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
    private final AtomicInteger totalItemsProcessed = new AtomicInteger(0);
    private final AtomicInteger totalFoldersScanned = new AtomicInteger(0);


    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    // Dependency Injection
    public FileService(RestTemplateBuilder restTemplateBuilder, FileRepository fileRepository, DriveRepository driveRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.fileRepository = fileRepository;
        this.driveRepository = driveRepository;
    }

    // Fetch all files for a given driveId. If fresh data exists in the database (updated within 24 hours), return it.
    // Otherwise, retrieve files recursively from Microsoft Graph API and update the database.
    @Cacheable(value = "files", key = "#driveId")
    public List<FileDTO> fetchAllFiles(String bearerToken, String driveId) {
        return fetchAllFiles(bearerToken, driveId, false);
    }

    // Internal method with forceRefresh parameter for scheduled jobs
    public List<FileDTO> fetchAllFiles(String bearerToken, String driveId, boolean forceRefresh) {
        logger.info("[CACHE MISS] Checking for files in drive: {}", driveId);
        LocalDateTime threshold = LocalDateTime.now().minusHours(24); // Define data freshness threshold

        // Check if recent data exists in the database for files in this drive
        // Skip this check if forceRefresh is true (used by scheduled jobs)
        if (!forceRefresh) {
            List<File> cachedFiles = fileRepository.findRecentFilesByDriveId(driveId, threshold);
            if (!cachedFiles.isEmpty()) {
                logger.info("[DATABASE HIT] Found {} fresh files in database (updated within 24h) - will be cached in memory", cachedFiles.size());
                
                // Log how many have flagReasons in database
                long filesWithFlags = cachedFiles.stream()
                        .filter(f -> f.getFlagReasons() != null && !f.getFlagReasons().isEmpty())
                        .count();
                logger.info("[DATABASE] {} out of {} files have flagReasons stored", filesWithFlags, cachedFiles.size());
                
                List<FileDTO> fileDTOs = cachedFiles.stream()
                        .map(this::mapToFileDTO)
                        .filter(dto -> !dto.getFlagReasons().isEmpty())
                        .toList();
                logger.info("[SUCCESS] Returning {} flagged files from database", fileDTOs.size());
                return fileDTOs;
            }
        } else {
            logger.info("[FORCE REFRESH] Skipping database cache check, fetching fresh data from API");
        }

        logger.info("[DATABASE MISS] No fresh data found in database. Fetching from Microsoft Graph API...");

        // Fetch files from Microsoft Graph API recursively
        List<FileDTO> files = new ArrayList<>();
        processedFileIds.clear();
        totalFilesSaved.set(0);
        fileNameCounts.clear();
        totalItemsProcessed.set(0);
        totalFoldersScanned.set(0);

        logger.info("[SCAN START] Beginning recursive scan from root folder...");
        
        // Start recursive fetch from root folder
        fetchFilesRecursively(bearerToken, driveId, "root", files);

        logger.info("[API SUCCESS] Collected {} files ≥15MB from Microsoft Graph API", files.size());
        logger.info("[SCAN COMPLETE] Scanned {} folders, processed {} items total", 
                totalFoldersScanned.get(), totalItemsProcessed.get());

        // Apply flags based on criteria
        applyFileFlags(files, driveId);

        // Save files to database
        int savedCount = saveFilesToDatabase(files, driveId);
        logger.info("[DATABASE] Saved {} files to database", savedCount);

        // Filter to return only flagged files
        List<FileDTO> flaggedFiles = files.stream()
                .filter(dto -> !dto.getFlagReasons().isEmpty())
                .toList();

        logger.info("[SUCCESS] Returning {} flagged files out of {} total files", flaggedFiles.size(), files.size());
        return flaggedFiles;
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
        int foldersScanned = totalFoldersScanned.incrementAndGet();
        
        // Log progress every 10 folders
        if (foldersScanned % 10 == 0) {
            logger.info("[PROGRESS] Scanned {} folders, processed {} items, collected {} files ≥15MB", 
                    foldersScanned, totalItemsProcessed.get(), files.size());
        }
        while (url != null) {
            pageCount++;
            // Use the helper with retry logic
            ResponseEntity<GraphFilesResponse> response = exchangeWithRetry(url, HttpMethod.GET, requestEntity);
    
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GraphFilesResponse body = response.getBody();
                List<GraphFilesResponse.Item> items = body.getValue();
    
                if (items != null && !items.isEmpty()) {
                    totalItemsProcessed.addAndGet(items.size());
                    if (pageCount == 1) {
                        logger.debug("[FOLDER] Processing folder with {} items (total collected: {})", items.size(), files.size());
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
                    // Process all items - will update if exists, insert if new
                    processItem(item, bearerToken, driveId, files);
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
            
            // Track filename for duplicate detection
            String fileName = item.getName().toLowerCase();
            fileNameCounts.compute(fileName, (k, v) -> (v == null) ? 1 : v + 1);
            
            // Add the file to the list in a thread-safe manner
            synchronized (files) {
                files.add(fileDTO);
            }
        }
    }

    // Apply flags to files based on size, age, and duplicate criteria
    private void applyFileFlags(List<FileDTO> files, String driveId) {
        long largeFileThreshold = 50L * 1024 * 1024; // 50 MB
        LocalDateTime fiveYearsAgo = LocalDateTime.now().minusYears(5);

        for (FileDTO file : files) {
            // Flag large files (>50MB)
            if (file.getSize() > largeFileThreshold) {
                file.getFlagReasons().add("large");
            }

            // Flag old files (>5 years)
            if (file.getLastModifiedDateTime() != null && 
                file.getLastModifiedDateTime().isBefore(fiveYearsAgo)) {
                file.getFlagReasons().add("old");
            }

            // Flag duplicates (same filename appears multiple times)
            String fileName = file.getName().toLowerCase();
            if (fileNameCounts.getOrDefault(fileName, 0) > 1) {
                file.getFlagReasons().add("duplicate");
            }
        }
        
        logger.info("[FLAGS] Applied flags to {} files", files.size());
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
                // Save flag reasons as comma-separated string
                if (!dto.getFlagReasons().isEmpty()) {
                    entity.setFlagReasons(String.join(",", dto.getFlagReasons()));
                }
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
        FileDTO dto = new FileDTO(
                entity.getId(),
                entity.getName(),
                entity.getSize(),
                entity.getWebUrl(),
                entity.getLastModifiedDateTime(),
                entity.getLastModifiedByDisplayName(),
                entity.getDriveId()
        );
        
        // Parse flagReasons from comma-separated string to list
        if (entity.getFlagReasons() != null && !entity.getFlagReasons().isEmpty()) {
            String[] reasons = entity.getFlagReasons().split(",");
            for (String reason : reasons) {
                dto.getFlagReasons().add(reason.trim());
            }
        }
        
        return dto;
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
        
        // Count only files with flags (stored in database)
        long totalCount = 0;
        for (String driveId : driveIds) {
            List<File> files = fileRepository.findRecentFilesByDriveId(driveId, threshold);
            totalCount += files.stream()
                    .filter(f -> f.getFlagReasons() != null && !f.getFlagReasons().isEmpty())
                    .count();
        }
        
        return totalCount;
    }

    // Check if drive cache is stale (for hourly incremental sync)
    public boolean isDriveCacheStale(String driveId, LocalDateTime threshold) {
        List<File> recentFiles = fileRepository.findRecentFilesByDriveId(driveId, threshold);
        return recentFiles.isEmpty();
    }

    /**
     * REMOVED: syncFilesWithDelta method - delta sync functionality removed after rollback
     * Using recursive fetch instead for reliability
     */

}




