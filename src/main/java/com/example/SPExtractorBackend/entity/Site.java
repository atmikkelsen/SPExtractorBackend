package com.example.SPExtractorBackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = "files")
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "site")
public class Site {
    @Id
    private String id;
    private String name;
    private String webUrl;
    private String displayName;
    private LocalDateTime lastUpdated;
}

