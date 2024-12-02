package com.example.SPExtractorBackend.response;

import com.azure.core.annotation.Get;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class GraphDrivesResponse {
    private List<Drive> value;

    @Getter
    @Setter
    public static class Drive {
        private String id;
        private String name;
        private String webUrl;
        private LocalDateTime lastModifiedDateTime;
    }
}
