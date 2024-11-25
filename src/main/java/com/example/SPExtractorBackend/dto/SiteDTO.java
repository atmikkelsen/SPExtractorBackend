package com.example.SPExtractorBackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteDTO {
    private String id;
    private String name;
    private String webUrl;
    private String displayName;

}
