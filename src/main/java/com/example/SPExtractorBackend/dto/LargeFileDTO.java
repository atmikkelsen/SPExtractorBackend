package com.example.SPExtractorBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter

public class LargeFileDTO {
    private String name;
    private int sizeInMB;
    private String libraryName;
    private String fileUrl;
    private String owner;
    private LocalDateTime createdTime;

    public LargeFileDTO(String name, int sizeInMB, String libraryName, String fileUrl, String owner, LocalDateTime createdTime) {
        this.name = name;
        this.sizeInMB = sizeInMB;
        this.libraryName = libraryName;
        this.fileUrl = fileUrl;
        this.owner = owner;
        this.createdTime = createdTime;
    }

}
