package com.example.SPExtractorBackend.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GraphSitesResponse {
    // Getter and Setter
    private List<Site> value;
    @Getter
    @Setter
    public static class Site {
        // Getters and Setters
        private String id;
        private String name;
        private String webUrl;
        private String displayName;
    }

}


