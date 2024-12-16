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
    private String siteId;
    private String siteName;

    public DriveDTO(String id, String name, String webUrl, LocalDateTime lastModifiedDateTime, String siteId, String siteName) {
        this.id = id;
        this.name = name;
        this.webUrl = webUrl;
        this.lastModifiedDateTime = lastModifiedDateTime;
        this.siteId = siteId;
        this.siteName = siteName;
    }

}


