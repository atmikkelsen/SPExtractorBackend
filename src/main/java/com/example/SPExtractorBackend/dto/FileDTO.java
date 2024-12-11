package com.example.SPExtractorBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter

public class FileDTO {
    private String name;
    private long size;
    private String webUrl;
    private LocalDateTime lastModifiedDateTime;
    private String lastModifiedByDisplayName;
    private String driveId;

    public FileDTO(String name, long size, String webUrl, LocalDateTime lastModifiedDateTime, String lastModifiedByDisplayName, String driveId) {
        this.name = name;
        this.size = size;
        this.webUrl = webUrl;
        this.lastModifiedDateTime = lastModifiedDateTime;
        this.lastModifiedByDisplayName = lastModifiedByDisplayName;
        this.driveId = driveId;
    }

}
