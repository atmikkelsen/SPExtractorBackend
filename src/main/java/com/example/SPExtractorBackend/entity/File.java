package com.example.SPExtractorBackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class File {
    @Id
    private String id;
    private String name;
    private long size;
    @Column(name = "web_url", columnDefinition = "TEXT")
    private String webUrl;
    private LocalDateTime lastModifiedDateTime;
    private String lastModifiedByDisplayName;
    private String driveId;
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;




}
