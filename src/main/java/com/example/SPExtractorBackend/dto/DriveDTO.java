package com.example.SPExtractorBackend.dto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DriveDTO {
    private String id;
    private String name;
    private String webUrl;
    private LocalDateTime lastModifiedDateTime;


    public DriveDTO(String id, String name, String webUrl, LocalDateTime lastModifiedDateTime) {
        this.id = id;
        this.name = name;
        this.webUrl = webUrl;
        this.lastModifiedDateTime = lastModifiedDateTime;
    }
}
