package com.example.SPExtractorBackend.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class GraphSitesResponse {
    private List<Site> value;

    // Getter and Setter
    public List<Site> getValue() {
        return value;
    }

    public void setValue(List<Site> value) {
        this.value = value;
    }

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


