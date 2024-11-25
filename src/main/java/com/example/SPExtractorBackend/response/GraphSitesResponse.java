package com.example.SPExtractorBackend.response;

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

    public static class Site {
        private String id;
        private String name;
        private String webUrl;
        private String displayName;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getWebUrl() {
            return webUrl;
        }

        public void setWebUrl(String webUrl) {
            this.webUrl = webUrl;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
