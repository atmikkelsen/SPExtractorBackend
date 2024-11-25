package com.example.SPExtractorBackend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class FileDTO {
    private String name; // Filens navn
    private int size; // Filens størrelse i bytes
    private String drive; // Bibliotek/Drive, hvor filen er placeret
    private String url; // URL til filen
    private LocalDateTime lastModified; // Sidste ændringstidspunkt
    private String owner; // Filens ejer
    private int siteId; // ID på det SharePoint-site, filen tilhører

    // Constructor
    public FileDTO(String name, int size, String drive, String url, LocalDateTime lastModified, String owner, int siteId) {
        this.name = name;
        this.size = size;
        this.drive = drive;
        this.url = url;
        this.lastModified = lastModified;
        this.owner = owner;
        this.siteId = siteId;
    }

    // Getters og setters
    // ...
}

