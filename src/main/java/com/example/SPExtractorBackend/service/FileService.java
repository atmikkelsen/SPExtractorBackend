package com.example.SPExtractorBackend.service;


import com.example.SPExtractorBackend.dto.FileDTO;
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

    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    @Autowired
    public FileService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    public List<FileDTO> fetchAllFiles(String bearerToken, String driveId) {
        List<FileDTO> files = new ArrayList<>();
        fetchFilesRecursively(bearerToken, driveId, "/root", files);
        return files;
    }

    private void fetchFilesRecursively(String bearerToken, String driveId, String itemId, List<FileDTO> files) {
        String url = graphApiBaseUrl + "/drives/" + driveId + "/items/" + itemId + "/children";
        System.out.println("Fetching from URL: " + url);

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
                    System.out.println("Items fetched: " + items.size());

                    // Process items in batches for concurrent execution
                    processItemsInBatches(items, bearerToken, driveId, files);
                } else {
                    System.out.println("No items found in this response.");
                }

                // Get the next page URL for pagination
                url = body.getNextLink(); // `getNextLink()` should return `@odata.nextLink` if available
            } else {
                throw new RuntimeException("Failed to fetch files from Microsoft Graph API for item: " + itemId);
            }
        }
    }

    private void processItemsInBatches(List<GraphFilesResponse.Item> items, String bearerToken, String driveId, List<FileDTO> files) {
        final int batchSize = 10; // Number of items per batch
        ExecutorService executor = Executors.newFixedThreadPool(5); // Limit concurrent threads
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < items.size(); i += batchSize) {
            int fromIndex = i;
            int toIndex = Math.min(i + batchSize, items.size());

            // Process a batch of items concurrently
            futures.add(CompletableFuture.runAsync(() -> {
                List<GraphFilesResponse.Item> batch = items.subList(fromIndex, toIndex);
                for (GraphFilesResponse.Item item : batch) {
                    processItem(item, bearerToken, driveId, files);
                }
            }, executor));
        }

        // Wait for all batches to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
    }

    private void processItem(GraphFilesResponse.Item item, String bearerToken, String driveId, List<FileDTO> files) {
        if (item.isFolder()) {
            // Recurse into subfolders
            System.out.println("Folder detected: " + item.getName());
            fetchFilesRecursively(bearerToken, driveId, item.getId(), files);
        } else if (item.getSize() > 15 * 1024 * 1024 ||
                item.getLastModifiedDateTime().isBefore(LocalDateTime.now().minusDays(365))) {
            // Add the file to the list if it meets the criteria
            synchronized (files) { // Synchronize to avoid concurrency issues
                files.add(new FileDTO(
                        item.getName(),
                        item.getSize(),
                        item.getWebUrl(),
                        item.getLastModifiedDateTime(),
                        item.getLastModifiedBy().getUser().getDisplayName()
                ));
            }
        }
    }


//
//    private void fetchFilesRecursively(String bearerToken, String driveId, String itemId, List<FileDTO> files) {
//        String url = graphApiBaseUrl + "/drives/" + driveId + "/items/" + itemId + "/children";
//        System.out.println("Fetching from URL: " + url);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(bearerToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//
//        while (url != null) {
//            ResponseEntity<GraphFilesResponse> response = restTemplate.exchange(
//                    url, HttpMethod.GET, requestEntity, GraphFilesResponse.class);
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                GraphFilesResponse body = response.getBody();
//                List<GraphFilesResponse.Item> items = body.getValue();
//
//                if (items != null) {
//                    System.out.println("Items fetched: " + items.size());
//                    for (GraphFilesResponse.Item item : items) {
//                        System.out.println("Processing item: " + item.getName());
//                        if (item.isFolder()) {
//                            // Recursively fetch items in the folder
//                            fetchFilesRecursively(bearerToken, driveId, item.getId(), files);
//                        } else if (item.getSize() > 15 * 1024 * 1024 ||
//                                item.getLastModifiedDateTime().isBefore(LocalDateTime.now().minusDays(365))) {
//                            // Add the file to the list if it meets the criteria
//                            files.add(new FileDTO(
//                                    item.getName(),
//                                    item.getSize(),
//                                    item.getWebUrl(),
//                                    item.getLastModifiedDateTime(),
//                                    item.getLastModifiedBy().getUser().getDisplayName()
//                            ));
//                        }
//                    }
//                } else {
//                    System.out.println("No items found in this response.");
//                }
//
//                // Update the URL to fetch the next page
//                url = body.getNextLink(); // `getNextLink` should return `@odata.nextLink`
//            } else {
//                throw new RuntimeException("Failed to fetch files from Microsoft Graph API for item: " + itemId);
//            }
//        }
//    }



}
