package com.example.SPExtractorBackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
public class Drive {
    @Id
    private String id;
    private String name;
    private String webUrl;
    private LocalDateTime lastModifiedDateTime;
    private String siteId;
    private String siteName;

}

