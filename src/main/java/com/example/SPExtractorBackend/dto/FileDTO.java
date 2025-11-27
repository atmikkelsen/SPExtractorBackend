package com.example.SPExtractorBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

public class FileDTO {
    private String id;
    private String name;
    private long size;
    private String webUrl;
    private LocalDateTime lastModifiedDateTime;
    private String lastModifiedByDisplayName;
    private String driveId;
    private List<String> flagReasons = new ArrayList<>();

    public FileDTO(String id, String name, long size, String webUrl, LocalDateTime lastModifiedDateTime, String lastModifiedByDisplayName, String driveId) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.webUrl = webUrl;
        this.lastModifiedDateTime = lastModifiedDateTime;
        this.lastModifiedByDisplayName = lastModifiedByDisplayName;
        this.driveId = driveId;
    }

}
