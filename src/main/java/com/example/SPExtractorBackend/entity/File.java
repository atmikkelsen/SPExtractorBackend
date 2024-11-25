package com.example.SPExtractorBackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = "site")
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // or another strategy as per your DB requirements

    private int id;
    private String name;
    private Long size;
    private String drive;
    private String url;
    private LocalDateTime lastModified;
    private String owner;
    private Long siteId;


}
