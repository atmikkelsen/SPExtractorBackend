package com.example.SPExtractorBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter

public class FileDTO {
    private String name;
    private int size;
    private String webUrl;
    private LocalDateTime lastModifiedDateTime;
    private String lastModifiedByDisplayName;

    public FileDTO(String name, int size, String webUrl, LocalDateTime lastModifiedDateTime, String lastModifiedByDisplayName) {
        this.name = name;
        this.size = size;
        this.webUrl = webUrl;
        this.lastModifiedDateTime = lastModifiedDateTime;
        this.lastModifiedByDisplayName = lastModifiedByDisplayName;
    }

}
